/*
 * Copyright (C) 2022-2024 The Project Lombok Authors.
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Download eclipse bundles.
 */
public class DownloadEclipseDependencies {
	
	public static void main(String[] args) throws Exception {
		String target = args[0];
		String eclipseVersion = args[1];
		String updateSiteUrl = args[2];
		boolean resolveDependencies = Boolean.parseBoolean(args[3]);
		List<String> bundles = Arrays.asList(Arrays.copyOfRange(args, 4, args.length));
		
		UpdateSite updateSite = new UpdateSite();
		updateSite.read(updateSiteUrl);
		
		final Set<String> artifacts;
		if (resolveDependencies) {
			artifacts = updateSite.resolveWithDependencies(bundles);
		} else {
			artifacts = updateSite.resolveWithoutDependencies(bundles);
		}
		
		String pluginTarget = target + "/" + eclipseVersion + "/plugins/";
		String pluginSource = updateSite.getResolvedUrl() + "/plugins/";
		
		for (String artifact : artifacts) {
			// Download artifact
			downloadFile(artifact, pluginSource, pluginTarget);
			
			// Download artifact source
			int index = artifact.lastIndexOf("_");
			String source = artifact.substring(0, index) + ".source" + artifact.substring(index);
			try {
				downloadFile(source, pluginSource, pluginTarget);
			} catch (Exception e) {
				// It's just the source; sometimes these aren't present (specifically, `org.eclipse.swt` doesn't currently appear to have the sources, at least not using the `_sources` naming scheme). Don't fail, just skip them.
				System.out.println("[failed]");
			}
		}
		
		writeEclipseLibrary(target, eclipseVersion);
	}
	
	private static void downloadFile(String filename, String repositoryUrl, String target) throws IOException {
		new File(target).mkdirs();
		File targetFile = new File(target, filename);
		if (targetFile.exists()) {
			System.out.println("File '" + filename + "' already exists");
			return;
		}
		System.out.print("Downloading '" + filename + "'... ");
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = getStreamForUrl(repositoryUrl + filename);
			out = new FileOutputStream(targetFile);
			
			copyZipButStripSignatures(in, out);
			System.out.println("[done]");
		} finally {
			try {
				if (in != null) in.close();
			} finally {
				if (out != null) out.close();
			}
		}
	}
	
	private static void copyZipButStripSignatures(InputStream rawIn, OutputStream rawOut) throws IOException {
		ZipInputStream in = null;
		ZipOutputStream out = null;
		
		in = new ZipInputStream(rawIn);
		out = new ZipOutputStream(rawOut);
		
		ZipEntry zipEntry;
		while ((zipEntry = in.getNextEntry()) != null) {
			if (zipEntry.getName().matches("META-INF/.*\\.(SF|RSA)")) continue;
			out.putNextEntry(zipEntry);
			copy(in, out);
		}
		out.close(); // zip streams buffer.
	}
	
	private static void copy(InputStream from, OutputStream to) throws IOException {
		byte[] b = new byte[4096];
		while (true) {
			int r = from.read(b);
			if (r == -1) return;
			to.write(b, 0, r);
		}
	}
	
	private static InputStream getStreamForUrl(String url) throws IOException, MalformedURLException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "lombok");
		connection.setRequestProperty("Accept", "*/*");
		return new BufferedInputStream(connection.getInputStream());
	}
	
	private static void writeEclipseLibrary(String target, String eclipseVersion) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		sb.append("<eclipse-userlibraries version=\"2\">\n");
		sb.append("<library name=\"");
		sb.append(eclipseVersion);
		sb.append("\" systemlibrary=\"false\">\n");
		
		File[] files = new File(new File(target, eclipseVersion), "plugins").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
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
		
		OutputStream out = null;
		try {
			out = new FileOutputStream(new File(target, eclipseVersion + ".userlibraries"));
			copy(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)), out);
		} finally {
			if (out != null) out.close();
		}
	}
}
