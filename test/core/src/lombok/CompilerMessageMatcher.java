package lombok;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilerMessageMatcher {
	/** Line Number (starting at 1) */
	private final long line;
	
	/** Position is either column number, OR position in file starting from the first byte. */
	private final long position;
	private final Collection<String> messageParts;
	
	public CompilerMessageMatcher(long line, long position, String message) {
		this.line = line;
		this.position = position;
		this.messageParts = Arrays.asList(message.split("\\s+"));
	}
	
	@Override public String toString() {
		StringBuilder parts = new StringBuilder();
		for (String part : messageParts) parts.append(part).append(" ");
		if (parts.length() > 0) parts.setLength(parts.length() - 1);
		return String.format("%d:%d %s", line, position, parts);
	}
	
	public boolean matches(CompilerMessage message) {
		if (message.line != this.line) return false;
		if (message.position != this.position) return false;
		for (String token : messageParts) {
			if (!message.message.contains(token)) return false;
		}
		return true;
	}
	
	public static List<CompilerMessageMatcher> readAll(InputStream rawIn) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(rawIn, "UTF-8"));
		List<CompilerMessageMatcher> out = new ArrayList<CompilerMessageMatcher>();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			CompilerMessageMatcher cmm = read(line);
			if (cmm != null) out.add(cmm);
		}
		return out;
	}
	
	private static final Pattern PATTERN = Pattern.compile("^(\\d+):(\\d+) (.*)$");
	private static CompilerMessageMatcher read(String line) {
		line = line.trim();
		if (line.isEmpty()) return null;
		Matcher m = PATTERN.matcher(line);
		if (!m.matches()) throw new IllegalArgumentException("Typo in test file: " + line);
		return new CompilerMessageMatcher(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), m.group(3));
	}
}
