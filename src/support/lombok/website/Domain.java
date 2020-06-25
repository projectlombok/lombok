package lombok.website;

import java.net.MalformedURLException;
import java.net.URL;

public class Domain {
	private static final String DEFAULT = "https://projectlombok.org/";
	private final String prefix;
	
	public Domain(String arg) {
		if (arg == null || arg.isEmpty()) this.prefix = DEFAULT;
		else {
			if (!arg.contains("://")) arg = "https://" + arg;
			if (!arg.endsWith("/")) arg += "/";
			this.prefix = arg;
		}
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public URL url(String path) throws MalformedURLException {
		return new URL(prefix + path);
	}
}
