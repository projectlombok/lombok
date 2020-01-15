package lombok.website;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchCurrentVersion {
	private FetchCurrentVersion() {}
	
	private static final Pattern VERSION_PATTERN = Pattern.compile("^.*<\\s*span\\s+id\\s*=\\s*[\"'](currentVersion|currentVersionFull)[\"'](?:\\s+style\\s*=\\s*[\"']display\\s*:\\s*none;?[\"'])?\\s*>\\s*([^\t<]+)\\s*<\\s*/\\s*span\\s*>.*$");
	
	public static void main(String[] args) throws IOException {
		System.out.print(fetchVersionFromSite(args.length == 0 || args[0].equals("full")));
	}
	
	public static String fetchVersionFromSite(boolean fetchFull) throws IOException {
		InputStream in = new URL("https://projectlombok.org/download").openStream();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			try {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					Matcher m = VERSION_PATTERN.matcher(line);
					if (m.matches() && m.group(1).equals("currentVersionFull") == fetchFull) return m.group(2).replace("&quot;", "\"");
				}
				throw new IOException("Expected a span with id 'currentVersion'");
			} finally {
				br.close();
			}
		} finally {
			in.close();
		}
	}
}
