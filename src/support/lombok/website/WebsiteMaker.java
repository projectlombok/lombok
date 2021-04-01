/*
 * Copyright (C) 2021 The Project Lombok Authors.
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
package lombok.website;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.java2html.Java2Html;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class WebsiteMaker {
	private final String version, fullVersion;
	private final File baseDir, outputDir;
	
	public WebsiteMaker(String version, String fullVersion, File baseDir, File outputDir) {
		this.version = version;
		this.fullVersion = fullVersion;
		this.baseDir = baseDir;
		this.outputDir = outputDir;
	}
	
	private static final class VersionFinder {
		public static String getVersion() {
			return getVersion0("getVersion");
		}
		
		public static String getFullVersion() {
			return getVersion0("getFullVersion");
		}
		
		private static String getVersion0(String mName) {
			try {
				Class<?> c = Class.forName("lombok.core.Version");
				Method m = c.getMethod(mName);
				return (String) m.invoke(null);
			} catch (ClassNotFoundException e) {
				System.err.println("You need to specify the version string, and the full version string, as first 2 arguments.");
				System.exit(1);
				return null;
			} catch (Exception e) {
				if (e instanceof RuntimeException) throw (RuntimeException) e;
				throw new RuntimeException(e);
			}
		}
	}
	
	private static void printAllVersions(Domain domain) throws Exception {
		List<List<String>> versions = readAllVersions(domain);
		for (List<String> v : versions) {
			System.out.println(" <a href=\"" + v.get(1) + "\">" + v.get(0) + "</a>");
		}
	}
	
	private static void buildAll(Domain domain, String version, String fullVersion, String argIn, String argOut, boolean newRelease) throws Exception {
		File in, out;
		if (argIn == null) {
			in = new File(".");
			if (new File(in, "build.xml").isFile() && new File(in, "website").isDirectory()) in = new File(in, "website");
		} else {
			in = new File(argIn);
		}
		
		if (argOut == null) {
			if (new File("./build.xml").isFile() && new File("./website").isDirectory() && new File("./build").isDirectory()) {
				out = new File("./build/website");
			} else {
				out = new File(in, "output");
			}
		} else {
			out = new File(argOut);
		}
		WebsiteMaker maker = new WebsiteMaker(version, fullVersion, in, out);
		maker.buildWebsite(domain, newRelease);
	}
	
	private static void buildChangelog(String version, String fullVersion, String argIn, String argOut) throws Exception {
		File in, out;
		if (argIn == null) {
			in = new File(".");
			if (new File(in, "build.xml").isFile() && new File(in, "website").isDirectory()) in = new File(in, "website");
		} else {
			in = new File(argIn);
		}
		
		if (argOut == null) {
			if (new File("./build.xml").isFile() && new File("./website").isDirectory() && new File("./build").isDirectory()) {
				out = new File("./build/website/changelog.html");
			} else {
				out = new File(in, "output/changelog.html");
			}
		} else {
			out = new File(argOut);
		}
		WebsiteMaker maker = new WebsiteMaker(version, fullVersion, in, out.getParentFile());
		maker.buildChangelog(out);
	}
	
	private static void buildDownloadEdge(String version, String fullVersion, String argIn, String argOut) throws Exception {
		File in, out;
		if (argIn == null) {
			in = new File(".");
			if (new File(in, "build.xml").isFile() && new File(in, "website").isDirectory()) in = new File(in, "website");
		} else {
			in = new File(argIn);
		}
		
		if (argOut == null) {
			if (new File("./build.xml").isFile() && new File("./website").isDirectory() && new File("./build").isDirectory()) {
				out = new File("./build/website-edge/download-edge.html");
			} else {
				out = new File(in, "output/download-edge.html");
			}
		} else {
			out = new File(argOut);
		}
		WebsiteMaker maker = new WebsiteMaker(version, fullVersion, in, out.getParentFile());
		maker.buildDownloadEdge(out);
	}
	
	private static void buildChangelogLatest(String version, String fullVersion, String argIn, String argOut) throws Exception {
		File in, out;
		if (argIn == null) {
			in = new File(".");
			if (new File(in, "build.xml").isFile() && new File(in, "website").isDirectory()) in = new File(in, "website");
		} else {
			in = new File(argIn);
		}
		
		if (argOut == null) {
			if (new File("./build.xml").isFile() && new File("./website").isDirectory() && new File("./build").isDirectory()) {
				out = new File("./build/latestchanges.html");
			} else {
				out = new File(in, "output/latestchanges.html");
			}
		} else {
			out = new File(argOut);
		}
		WebsiteMaker maker = new WebsiteMaker(version, fullVersion, in, out.getParentFile());
		maker.buildChangelogLatest(out);
	}
	
	public static void main(String[] args) throws Exception {
		String version, fullVersion;
		Domain domain = new Domain(args.length < 1 ? "" : args[0]);
		
		if (args.length < 3) {
			version = VersionFinder.getVersion();
			fullVersion = VersionFinder.getFullVersion();
		} else {
			version = args[1];
			fullVersion = args[2];
		}
		
		String argIn = args.length < 5 ? null : args[4];
		String argOut = args.length < 6 ? null : args[5];
		if (args.length < 4 || args[3].equalsIgnoreCase("all")) {
			buildAll(domain, version, fullVersion, argIn, argOut, false);
		} else if (args.length < 4 || args[3].equalsIgnoreCase("all-newrelease")) {
			buildAll(domain, version, fullVersion, argIn, argOut, true);
		} else if (args[3].equalsIgnoreCase("changelog")) {
			buildChangelog(version, fullVersion, argIn, argOut);
		} else if (args[3].equalsIgnoreCase("download-edge")) {
			buildDownloadEdge(version, fullVersion, argIn, argOut);
		} else if (args[3].equalsIgnoreCase("changelog-latest")) {
			buildChangelogLatest(version, fullVersion, argIn, argOut);
		} else if (args[3].equalsIgnoreCase("print-allversions")) {
			printAllVersions(domain);
		} else {
			throw new IllegalArgumentException("4th argument must be one of 'all', 'changelog', 'download-edge', 'changelog-latest'");
		}
	}
	
	private Configuration makeFreemarkerConfig() throws IOException {
		Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_25);
		freemarkerConfig.setEncoding(Locale.ENGLISH, "UTF-8");
		freemarkerConfig.setOutputEncoding("UTF-8");
		freemarkerConfig.setOutputFormat(HTMLOutputFormat.INSTANCE);
		freemarkerConfig.setTemplateLoader(createLoader());
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		return freemarkerConfig;
	}
	
	public void buildChangelog(File out) throws Exception {
		Configuration freemarkerConfig = makeFreemarkerConfig();
		outputDir.mkdirs();
		convertChangelog(freemarkerConfig, out);
	}
	
	public void buildChangelogLatest(File out) throws Exception {
		outputDir.mkdirs();
		String htmlForLatest = CompileChangelog.getHtmlForLatest(baseDir.getParentFile(), version);
		FileOutputStream fos = new FileOutputStream(out);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bw.write(htmlForLatest);
			bw.close();
		} finally {
			fos.close();
		}
	}
	
	public void buildDownloadEdge(File out) throws Exception {
		Configuration freemarkerConfig = makeFreemarkerConfig();
		
		outputDir.mkdirs();
		convertDownloadEdge(freemarkerConfig, out);
	}
	
	public void buildHtAccess(File out) throws Exception {
		Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_25);
		freemarkerConfig.setEncoding(Locale.ENGLISH, "UTF-8");
		freemarkerConfig.setOutputEncoding("UTF-8");
		freemarkerConfig.setOutputFormat(HTMLOutputFormat.INSTANCE);
		freemarkerConfig.setTemplateLoader(createLoader("extra"));
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		
		outputDir.mkdirs();
		convertHtAccess(freemarkerConfig, out);
	}
	
	public void buildWebsite(Domain domain, boolean newRelease) throws Exception {
		Configuration freemarkerConfig = makeFreemarkerConfig();
		
		outputDir.mkdirs();
		convertTemplates(domain, freemarkerConfig, newRelease);
		buildHtAccess(new File(outputDir, ".htaccess"));
	}
	
	private TemplateLoader createLoader() throws IOException {
		return createLoader("templates");
	}
	
	private TemplateLoader createLoader(String base) throws IOException {
		return new FileTemplateLoader(new File(baseDir, base));
	}
	
	private void convertHtAccess(Configuration freemarker, File outFile) throws Exception {
		Map<String, Object> dataModel = new HashMap<String, Object>();
		dataModel.put("setupPages", listHtmlNames(new File(outputDir, "setup")));
		dataModel.put("featurePages", listHtmlNames(new File(outputDir, "features")));
		dataModel.put("experimentalPages", listHtmlNames(new File(outputDir, "features/experimental")));
		Template template = freemarker.getTemplate("htaccess");
		FileOutputStream fileOut = new FileOutputStream(outFile);
		try {
			Writer wr = new BufferedWriter(new OutputStreamWriter(fileOut, "UTF-8"));
			template.process(dataModel, wr);
			wr.close();
		} finally {
			fileOut.close();
		}
	}
	
	private List<String> listHtmlNames(File dir) {
		List<String> out = new ArrayList<String>();
		for (String s : dir.list()) {
			if (s.endsWith(".html") && !s.equals("index.html")) out.add(s.substring(0, s.length() - 5));
		}
		return out;
	}
	
	private void convertChangelog(Configuration freemarker, File outFile) throws Exception {
		Map<String, Object> dataModel = createBasicDataModel();
		
		Template template = freemarker.getTemplate("changelog.html");
		FileOutputStream fileOut = new FileOutputStream(outFile);
		try {
			Writer wr = new BufferedWriter(new OutputStreamWriter(fileOut, "UTF-8"));
			template.process(dataModel, wr);
			wr.close();
		} finally {
			fileOut.close();
		}
	}
	
	private void convertDownloadEdge(Configuration freemarker, File outFile) throws Exception {
		Map<String, Object> dataModel = createBasicDataModel();
		
		Template template = freemarker.getTemplate("_download-edge.html");
		FileOutputStream fileOut = new FileOutputStream(outFile);
		try {
			Writer wr = new BufferedWriter(new OutputStreamWriter(fileOut, "UTF-8"));
			template.process(dataModel, wr);
			wr.close();
		} finally {
			fileOut.close();
		}
	}
	
	private void convertTemplates(Domain domain, Configuration freemarker, boolean newRelease) throws Exception {
		File basePagesLoc = new File(baseDir, "templates");
		Map<String, Object> dataModel = createBasicDataModel();
		dataModel.putAll(createExtendedDataModel(domain, newRelease));
		convertTemplates_(freemarker, "", basePagesLoc, outputDir, 0, dataModel);
	}
	
	private void convertTemplates_(Configuration freemarker, String prefix, File from, File to, int depth, Map<String, Object> dataModel) throws Exception {
		if (depth > 50) throw new IllegalArgumentException("50 levels is too deep: " + from);
		
		for (File f : from.listFiles()) {
			if (f.isDirectory()) convertTemplates_(freemarker, prefix + f.getName() + "/", f, new File(to, f.getName()), depth + 1, dataModel);
			if (!f.isFile() || f.getName().startsWith("_")) continue;
			to.mkdirs();
			Template template = freemarker.getTemplate(prefix + f.getName());
			FileOutputStream fileOut = new FileOutputStream(new File(to, f.getName()));
			try {
				Writer wr = new BufferedWriter(new OutputStreamWriter(fileOut, "UTF-8"));
				template.process(dataModel, wr);
				wr.close();
			} finally {
				fileOut.close();
			}
		}
	}
	
	private Map<String, Object> createBasicDataModel() throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String currentTime = sdf.format(new Date());
		
		data.put("version", version);
		data.put("fullVersion", fullVersion);
		data.put("timestampString", currentTime);
		data.put("year", "" + new GregorianCalendar().get(Calendar.YEAR));
		data.put("changelog", CompileChangelog.getHtmlStartingAtSection(baseDir.getParentFile(), version));
		data.put("changelogEdge", CompileChangelog.getHtmlForEdge(baseDir.getParentFile(), version));
		
		return data;
	}
	
	private static final Pattern LOMBOK_LINK = Pattern.compile("^.*<a(?: (?:id|class|rel|rev|download|target|type)(?:=\"[^\"]*\")?)* href=\"(downloads/[^\"]+)\"(?: (?:id|class|rel|rev|download|target|type)(?:=\"[^\"]*\")?)*>([^<]+)</a>.*$");
	private Map<String, Object> createExtendedDataModel(Domain domain, boolean newRelease) throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("usages", new HtmlMaker(new File(baseDir, "usageExamples")));
		List<List<String>> allVersions = readAllVersions(domain);
		if (!newRelease && !allVersions.isEmpty()) allVersions.remove(0); // remove current version; it will be 're-added' as current version automatically.
		data.put("linksToVersions", allVersions);
		
		return data;
	}
	
	private static List<List<String>> readAllVersions(Domain domain) throws IOException {
		InputStream in = domain.url("all-versions.html").openStream();
		ArrayList<List<String>> links = new ArrayList<List<String>>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				Matcher m = LOMBOK_LINK.matcher(line);
				if (m.matches()) {
					String url = m.group(1);
					String name = m.group(2);
					if (name.endsWith(" [Current Version]")) {
						name = "lombok-" + name.substring(0, name.length() - " [Current Version]".length()) + ".jar";
						url = url.replace("lombok.jar", name);
					}
					links.add(Arrays.asList(name, url));
				}
			}
		} finally {
			in.close();
		}
		
		return links;
	}
	
	public static class HtmlMaker {
		private final File usagesDir;
		
		HtmlMaker(File usagesDir) {
			this.usagesDir = usagesDir;
		}
		
		public String pre(String name) throws IOException {
			return convert(new File(usagesDir, name + "Example_pre.jpage"));
		}
		
		public String post(String name) throws IOException {
			return convert(new File(usagesDir, name + "Example_post.jpage"));
		}
		
		public String convert(File file) throws IOException {
			String rawJava = readFully(file);
			return Java2Html.convertToHtml(rawJava);
		}
	}
	
	public static String readFully(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			StringBuilder out = new StringBuilder();
			char[] b = new char[65536];
			while (true) {
				int r = isr.read(b);
				if (r == -1) break;
				out.append(b, 0, r);
			}
			return out.toString();
		} finally {
			fis.close();
		}
	}
}
