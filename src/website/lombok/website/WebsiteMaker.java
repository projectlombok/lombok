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
import java.net.URL;
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
	
	private static void buildAll(String version, String fullVersion, String argIn, String argOut) throws Exception {
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
		maker.buildWebsite();
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
		
		if (args.length < 2) {
			version = VersionFinder.getVersion();
			fullVersion = VersionFinder.getFullVersion();
		} else {
			version = args[0];
			fullVersion = args[1];
		}
		
		if (args.length < 3 || args[2].equalsIgnoreCase("all")) {
			buildAll(version, fullVersion, args.length < 4 ? null : args[3], args.length < 5 ? null : args[4]);
		} else if (args[2].equalsIgnoreCase("changelog")) {
			buildChangelog(version, fullVersion, args.length < 4 ? null : args[3], args.length < 5 ? null : args[4]);
		} else if (args[2].equalsIgnoreCase("download-edge")) {
			buildDownloadEdge(version, fullVersion, args.length < 4 ? null : args[3], args.length < 5 ? null : args[4]);
		} else if (args[2].equalsIgnoreCase("changelog-latest")) {
			buildChangelogLatest(version, fullVersion, args.length < 4 ? null : args[3], args.length < 5 ? null : args[4]);
		} else {
			throw new IllegalArgumentException("3rd argument must be one of 'all', 'changelog', 'download-edge', 'changelog-latest'");
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
	
	public void buildWebsite() throws Exception {
		Configuration freemarkerConfig = makeFreemarkerConfig();
		
		outputDir.mkdirs();
		convertTemplates(freemarkerConfig);
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
	
	private void convertTemplates(Configuration freemarker) throws Exception {
		File basePagesLoc = new File(baseDir, "templates");
		Map<String, Object> dataModel = createBasicDataModel();
		dataModel.putAll(createExtendedDataModel());
		convertTemplates_(freemarker, "", basePagesLoc, outputDir, 0, dataModel);
	}
	
	private void convertTemplates_(Configuration freemarker, String prefix, File from, File to, int depth, Map<String, Object> dataModel) throws Exception {
		if (depth > 50) throw new IllegalArgumentException("50 levels is too deep: " + from);
		
		for (File f : from.listFiles()) {
			if (f.isDirectory()) convertTemplates_(freemarker, prefix + f.getName() + "/", f, new File(to, f.getName()), depth + 1, dataModel);
			if (!f.isFile() || !f.getName().endsWith(".html") || f.getName().startsWith("_")) continue;
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
		data.put("changelog", CompileChangelog.getHtml(baseDir.getParentFile()));
		data.put("changelogEdge", CompileChangelog.getHtmlForEdge(baseDir.getParentFile(), version));
		
		return data;
	}
	
	private static final Pattern LOMBOK_LINK = Pattern.compile("^.*<a(?: (?:id|class|rel|rev|download|target|type)(?:=\"[^\"]*\")?)* href=\"(downloads/[^\"]+)\"(?: (?:id|class|rel|rev|download|target|type)(?:=\"[^\"]*\")?)*>([^<]+)</a>.*$");
	private Map<String, Object> createExtendedDataModel() throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("usages", new HtmlMaker(new File(baseDir, "usageExamples")));
		InputStream in = new URL("https://projectlombok.org/all-versions.html").openStream();
		ArrayList<List<String>> links = new ArrayList<List<String>>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				Matcher m = LOMBOK_LINK.matcher(line);
				if (m.matches()) links.add(Arrays.asList(m.group(1), m.group(2)));
			}
		} finally {
			in.close();
		}
		
		data.put("linksToVersions", links);
		
		return data;
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
