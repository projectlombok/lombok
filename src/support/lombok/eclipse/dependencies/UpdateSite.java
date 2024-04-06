/*
 * Copyright (C) 2024 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.dependencies;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import lombok.eclipse.dependencies.model.Child;
import lombok.eclipse.dependencies.model.Repository;
import lombok.eclipse.dependencies.model.Required;
import lombok.eclipse.dependencies.model.Unit;
import lombok.eclipse.dependencies.model.VersionRange;

public class UpdateSite {
	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	private static final String OSGI_OS = OS_NAME.contains("windows") ? "windows" : OS_NAME.contains("mac") ? "mac" : "linux";
	private static final String OS_ARCH = System.getProperty("os.arch");
	private static final String OSGI_ARCH = OS_ARCH.equals("aarch64") ? "aarch64" : "x86_64";
	
	private JAXBContext jaxbContext;
	private Repository repository;
	private String resolvedUrl;
	private SAXParserFactory saxParserFactory;
	
	public UpdateSite() throws Exception {
		jaxbContext = JAXBContext.newInstance(Repository.class);
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		spf.setXIncludeAware(false);
		saxParserFactory = spf;
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
			repository = unmarshalRepository(inputStream);
		}
	}
	
	public Set<String> resolveWithoutDependencies(List<String> dependencies) {
		return resolve(dependencies, false);
	}
	
	public Set<String> resolveWithDependencies(List<String> dependencies) {
		return resolve(dependencies, true);
	}
	
	private Set<String> resolve(List<String> dependencies, boolean withDependencies) {
		Queue<Required> toResolve = new ArrayDeque<>();
		for (String dependency : dependencies) {
			String[] split = dependency.split(":");
			Required required = new Required();
			required.namespace = split[0];
			required.name = split[1];
			required.range = VersionRange.ALL;
			toResolve.add(required);
		}
		Set<Unit> resolved = new HashSet<>();
		while (!toResolve.isEmpty()) {
			Required next = toResolve.poll();
			
			// Skip already resolved
			if (resolved.stream().anyMatch(u -> u.satisfies(next))) {
				continue;
			}
			
			List<Unit> satisfyingUnits = repository.units.stream().filter(u -> u.satisfies(next)).collect(Collectors.toList());
			// Skip unknown
			if (satisfyingUnits.isEmpty()) {
				System.out.println("Skipping unknown unit " + next);
				continue;
			}
			
			// Skip JDK dependencies
			boolean jdkDependency = satisfyingUnits.stream().anyMatch(u -> u.id.equals("a.jre.javase"));
			if (jdkDependency) {
				continue;
			}
			
			if (satisfyingUnits.size() > 1) {
				System.out.println("Ambiguous resolution for " + next + ": " + satisfyingUnits.toString() + ", picking first");
			}
			
			Unit unit = satisfyingUnits.get(0);
			resolved.add(unit);
			
			if (withDependencies && unit.requires != null) {
				for (Required required : unit.requires) {
					if (required.optional) continue;
					if (!matchesFilter(required.filter)) continue;
					
					toResolve.add(required);
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

	private String resolveNextChild(String currentUrl) throws Exception {
		try (InputStream inputStream = readJarOrXml(currentUrl, "compositeContent")) {
			Repository repository = unmarshalRepository(inputStream);
			Child lastChild = repository.children.get(repository.children.size() - 1);
			return lastChild.location;
		}
	}

	private Repository unmarshalRepository(InputStream inputStream) throws Exception {
		Source source = new SAXSource(saxParserFactory.newSAXParser().getXMLReader(), new InputSource(inputStream));
		Repository repository = (Repository) jaxbContext.createUnmarshaller().unmarshal(source);
		return repository;
	}
	
	private InputStream readJarOrXml(String url, String name) throws Exception {
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
	
	private static InputStream getStreamForUrl(String url) throws Exception {
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
