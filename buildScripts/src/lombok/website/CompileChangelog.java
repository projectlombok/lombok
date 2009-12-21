package lombok.website;

import com.petebevin.markdown.MarkdownProcessor;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompileChangelog {
	public static void main(String[] args) {
		String fileIn = args[0];
		String fileOut = args[1];
		boolean edge = args.length > 3 && "-edge".equals(args[2]);
		String version = edge ? args[3] : null;
		
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
			
			String result = edge ? buildEdge(markdown, version) : build(markdown);
			
			FileOutputStream file = new FileOutputStream(fileOut);
			file.write(result.getBytes("UTF-8"));
			file.close();
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static String build(String markdown) {
		return new MarkdownProcessor().markdown(markdown);
	}
	
	private static final Pattern LAST_CHANGELOG = Pattern.compile(
			"^.*### v$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static String buildEdge(String markdown, String version) {
		if (version.toUpperCase().endsWith("-HEAD") || version.toUpperCase().endsWith("-EDGE")) {
			version = version.substring(0, version.length() - 5);
		}
		
		Pattern p = Pattern.compile(
				"(?is-m)^.*###\\s*v" + version + ".*?\n(.*?)(?:###\\s*v.*)?$");
		Matcher m = p.matcher(markdown);
		String subMarkdown = m.matches() ? m.group(1) : "* No changelog records for this edge release.";
		return new MarkdownProcessor().markdown(subMarkdown);
	}
}
