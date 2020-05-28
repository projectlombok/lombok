package lombok.website;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.petebevin.markdown.MarkdownProcessor;

public class CompileChangelog {
	public static void main(String[] args) {
		String fileIn = args[0];
		String fileOut = args[1];
		boolean edge = args.length > 3 && "-edge".equals(args[2]);
		boolean latest = args.length > 3 && "-latest".equals(args[2]);
		String version = args.length > 3 ? args[3] : null;
		
		try {
			FileInputStream in = new FileInputStream(fileIn);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			byte[] b = new byte[65536];
			while (true) {
				int r = in.read(b);
				if ( r == -1 ) break;
				out.write(b, 0, r);
			}
			in.close();
			String markdown = new String(out.toByteArray(), "UTF-8");
			
			String result;
			if (edge) {
				result = buildEdge(sectionByVersion(markdown, version));
			} else if (latest) {
				result = buildLatest(sectionByVersion(markdown, version));
			} else {
				result = markdownToHtml(sectionStartingAt(markdown, version));
			}
			
			FileOutputStream file = new FileOutputStream(fileOut);
			file.write(result.getBytes("UTF-8"));
			file.close();
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static String getHtmlForEdge(File root, String edgeVersion) throws IOException {
		File f = new File(root, "doc/changelog.markdown");
		String raw = readFile(f);
		return buildEdge(sectionByVersion(raw, edgeVersion));
	}
	
	public static String getHtmlForLatest(File root, String latestVersion) throws IOException {
		File f = new File(root, "doc/changelog.markdown");
		String raw = readFile(f);
		return buildLatest(sectionByVersion(raw, latestVersion));
	}
	
	public static String getHtml(File root) throws IOException {
		File f = new File(root, "doc/changelog.markdown");
		String raw = readFile(f);
		return markdownToHtml(raw);
	}
	
	public static String getHtmlStartingAtSection(File root, String version) throws IOException {
		File f = new File(root, "doc/changelog.markdown");
		String raw = readFile(f);
		return markdownToHtml(sectionStartingAt(raw, version));
	}
	
	private static String readFile(File f) throws IOException {
		byte[] b = new byte[65536];
		FileInputStream in = new FileInputStream(f);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (true) {
				int r = in.read(b);
				if ( r == -1 ) break;
				out.write(b, 0, r);
			}
			in.close();
			return new String(out.toByteArray(), "UTF-8");
		} finally {
			in.close();
		}
	}
	
	private static String markdownToHtml(String markdown) {
		return new MarkdownProcessor().markdown(markdown);
	}
	
	private static String buildEdge(String section) {
		String latest = section != null ? section : "* No changelog records for this edge release.";
		return markdownToHtml(latest);
	}
	
	private static String buildLatest(String section) {
		String latest = section != null ? section : "* No changelog records for this release.";
		String noIssueLinks = latest.replaceAll("\\[[^]]*[Ii]ssue[^]]*\\]\\([^)]*\\)", "");
		String noLinks = noIssueLinks.replaceAll("\\[([^]]*)\\]\\([^)]*\\)", "$1");
		return markdownToHtml(noLinks);
	}
	
	private static String sectionStartingAt(String markdown, String version) {
		if (version.toUpperCase().endsWith("-HEAD") || version.toUpperCase().endsWith("-EDGE")) {
			version = version.substring(0, version.length() - 5);
		}
		
		Pattern p = Pattern.compile("^.*###\\s*v(.*)$");
		BufferedReader br = new BufferedReader(new StringReader(markdown));
		StringBuilder out = new StringBuilder();
		int state = 0;
		try {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (state < 2) {
					Matcher m = p.matcher(line);
					if (m.matches()) state = m.group(1).startsWith(version) ? 2 : 1;
				}
				if (state != 1) {
					out.append(line);
					out.append("\n");
				}
			}
			return out.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String sectionByVersion(String markdown, String version) {
		if (version.toUpperCase().endsWith("-HEAD") || version.toUpperCase().endsWith("-EDGE")) {
			version = version.substring(0, version.length() - 5);
		}
		
		Pattern p = Pattern.compile("(?is-m)^.*###\\s*v" + version + ".*?\n(.*?)(?:###\\s*v.*)?$");
		Matcher m = p.matcher(markdown);
		return m.matches() ? m.group(1) : null;
	}
}