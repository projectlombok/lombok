package lombok.eclipse.dependencies;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class UpdateSite {
	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	private static final String OSGI_OS = OS_NAME.contains("windows") ? "windows" : OS_NAME.contains("mac") ? "mac" : "linux";
	private static final String OS_ARCH = System.getProperty("os.arch");
	private static final String OSGI_ARCH = OS_ARCH.equals("aarch64") ? "aarch64" : "x86_64";
	
	private JAXBContext jaxbContext;
	private Repository repository;
	private Map<String, List<Unit>> providesIndex;
	private String resolvedUrl;
	
	public UpdateSite() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(Repository.class);
		providesIndex = new HashMap<>();
	}
	
	public void read(String url) throws Exception {
		String currentUrl = url;
		try {
			while (true) {
				String child = resolveNextChild(currentUrl);
				if (child.startsWith("https://")) {
					currentUrl = child;
				} else {
					currentUrl += child;
				}
			}
		} catch (FileNotFoundException e) {
			// Found the real repository
		}
		
		resolvedUrl = currentUrl;
		
		try (InputStream inputStream = readJarOrXml(resolvedUrl, "content")) {
			repository = (Repository) jaxbContext.createUnmarshaller().unmarshal(inputStream);
			
			// Build index
			for (Unit unit : repository.units) {
				for (Provided provides : unit.provides) {
					providesIndex.computeIfAbsent(provides.namespace + ":" + provides.name, k -> new ArrayList<>()).add(unit);
				}
			}
		}
		;
	}
	
	public Set<String> resolveWithoutDependencies(List<String> dependencies) {
		return resolve(dependencies, false);
	}
	
	public Set<String> resolveWithDependencies(List<String> dependencies) {
		return resolve(dependencies, true);
	}
	
	private Set<String> resolve(List<String> dependencies, boolean withDependencies) {
		Queue<String> toResolve = new UniqueQueue<>();
		for (String dependency : dependencies) {
			toResolve.add(dependency);
		}
		Set<Unit> resolved = new HashSet<>();
		while (!toResolve.isEmpty()) {
			String next = toResolve.poll();
			
			List<Unit> providedUnits = providesIndex.get(next);
			// Skip unknown
			if (providedUnits == null) {
				System.out.println("Skipping unknown unit " + next);
				continue;
			}
			// Remove a.jre.javase dependency
			List<Unit> filteredProvidedUnits = providedUnits.stream()
				.filter(u -> !u.id.equals("a.jre.javase")) // Remove
				.collect(Collectors.toList());
			
			if (filteredProvidedUnits.size() == 0) {
				// This is a JDK only dependency, skip
				continue;
			}
			
			// Skip ambiguous (we could use version ranges to solve that...)
			if (filteredProvidedUnits.size() > 1) {
				boolean alreadyResolved = filteredProvidedUnits.stream().anyMatch(resolved::contains);
				if (!alreadyResolved) {
					System.out.println("Ambiguous resolution for " + next + ": " + filteredProvidedUnits.toString());
					continue;
				}
			}
			
			Unit unit = filteredProvidedUnits.get(0);
			resolved.add(unit);
			
			if (withDependencies && unit.requires != null) {
				for (Required required : unit.requires) {
					if (required.optional) continue;
					if (!matchesFilter(required.filter)) continue;
					
					toResolve.add(required.namespace + ":" + required.name);
				}
			}
		}
		
		return resolved.stream().map(u -> u.toString() + ".jar").collect(Collectors.toSet());
	}
	
	// Dummy implementation
	private boolean matchesFilter(String filter) {
		if (filter == null) {
			return true;
		}
		if (filter.contains("osgi.arch=") && !filter.contains("osgi.arch=" + OSGI_ARCH)) {
			return false;
		}
		if (filter.contains("osgi.os=") && !filter.contains("osgi.os=" + OSGI_OS)) {
			return false;
		}
		return true;
	}

	private String resolveNextChild(String currentUrl) throws MalformedURLException, IOException, JAXBException {
		try (InputStream inputStream = readJarOrXml(currentUrl, "compositeContent")) {
			Repository repository = (Repository) jaxbContext.createUnmarshaller().unmarshal(inputStream);
			Child lastChild = repository.children.get(repository.children.size() - 1);
			return lastChild.location;
		}
	}
	
	private InputStream readJarOrXml(String url, String name) throws MalformedURLException, IOException {
		try {
			return getStreamForUrl(url + "/" + name + ".xml");
		} catch (FileNotFoundException e) {
			System.out.println("Not found, trying jar");
		}
		ZipInputStream zipInputStream = new ZipInputStream(getStreamForUrl(url + "/" + name + ".jar"));
		ZipEntry entry;
		while ((entry = zipInputStream.getNextEntry()) != null) {
			if (entry.getName().equals(name + ".xml")) {
				return zipInputStream;
			}
		}
		throw new FileNotFoundException();
	}
	
	private static InputStream getStreamForUrl(String url) throws IOException, MalformedURLException {
		System.out.println("Reading " + url);
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "lombok");
		connection.setRequestProperty("Accept", "*/*");
		InputStream in = new BufferedInputStream(connection.getInputStream());
		return in;
	}

	public String getResolvedUrl() {
		return resolvedUrl;
	}
}
