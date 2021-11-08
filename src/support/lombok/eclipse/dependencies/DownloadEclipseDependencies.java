package lombok.eclipse.dependencies;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Download eclipse bundles.
 */
public class DownloadEclipseDependencies {
	
	public static void main(String[] args) throws IOException {
		String target = args[0];
		String eclipseVersion = args[1];
		String updatePage = args[2];
		String[] packages = Arrays.copyOfRange(args, 3, args.length);
		
		String pluginTarget = target + "/" + eclipseVersion + "/plugins/";
		
		String indexData = readUrlAsString(updatePage);
		
		for (String pkg : packages) {
			Matcher matcher = Pattern.compile("(" + pkg.replace(".", "\\.") + "_.*?\\.jar)").matcher(indexData);
			if (matcher.find()) {
				String path = matcher.group(1);
				
				try {
					downloadFile(path, updatePage, pluginTarget);
				} catch (Exception e) {
				}
				try {
					int index = path.lastIndexOf("_");
					String source = path.substring(0, index) + ".source" + path.substring(index);
					downloadFile(source, updatePage, pluginTarget);
				} catch (Exception e) {
				}
			} else {
				System.out.println("Bundle \"" + pkg + "\" not found");
			}
		}
		
		writeEclipseLibrary(target, eclipseVersion);
	}
	
	private static String readUrlAsString(String url) throws MalformedURLException, IOException {
		InputStream in = getStreamForUrl(url);
		
		StringBuilder sb = new StringBuilder();
		
		int bufferSize = 1024;
		char[] buffer = new char[bufferSize];
		InputStreamReader reader = new InputStreamReader(in, "UTF-8");
		for (int count = 0;  (count = reader.read(buffer, 0, bufferSize)) > 0;) {
			sb.append(buffer, 0, count);
		}
		return sb.toString();
	}
	
	private static void downloadFile(String filename, String repositoryUrl, String target) throws IOException {
		Files.createDirectories(Paths.get(target));
		Path targetFile = Paths.get(target, filename);
		if (Files.exists(targetFile)) {
			System.out.println("File '" + filename + "' already exists");
			return;
		}
		System.out.print("Downloading '" + filename + "'... ");
		try {
			Files.copy(getStreamForUrl(repositoryUrl + filename), targetFile, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("[done]");
		} catch(IOException e) {
			System.out.println("[error]");
		}
	}
	
	private static InputStream getStreamForUrl(String url) throws IOException, MalformedURLException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "lombok");
		connection.setRequestProperty("Accept", "*/*");
		InputStream in = new BufferedInputStream(connection.getInputStream());
		return in;
	}

	private static void writeEclipseLibrary(String target, String eclipseVersion) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		sb.append("<eclipse-userlibraries version=\"2\">\n");
		sb.append("<library name=\"");
		sb.append(eclipseVersion);
		sb.append("\" systemlibrary=\"false\">\n");
		
		File[] files = new File(new File(target, eclipseVersion), "plugins").listFiles(new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".jar") && !name.contains(".source_");
			}
		});
		Arrays.sort(files);
		
		for (File file : files) {
			sb.append("<archive path=\"");
			sb.append(file.getAbsolutePath());
			sb.append("\"");
			
			String path = file.getAbsolutePath();
			int index = path.lastIndexOf("_");
			
			sb.append(" source=\"");
			sb.append(path.substring(0, index) + ".source" + path.substring(index));
			sb.append("\"");
			
			sb.append(" />\n");
		}
		
		sb.append("</library>\n");
		sb.append("</eclipse-userlibraries>\n");
		
		Files.writeString(Paths.get(target, eclipseVersion + ".userlibraries"), sb.toString());
	}
}
