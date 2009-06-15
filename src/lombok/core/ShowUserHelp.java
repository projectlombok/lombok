package lombok.core;

import java.awt.Desktop;
import java.net.URI;

/**
 * This class is used as main class for the lombok jar; this way, if the jar is run as java app, the user is pointed
 * at documentation about lombok.
 */
public class ShowUserHelp {
	private static final URI ABOUT_LOMBOK_URL = URI.create("http://wiki.github.com/rzwitserloot/lombok");
	
	public static void main(String[] args) {
		boolean browserOpened = false;
		try {
			Desktop.getDesktop().browse(ABOUT_LOMBOK_URL);
			browserOpened = true;
		} catch ( Exception ignore ) {}
		
		String version = Version.getVersion();
		final String nextStep = browserOpened ? "See your browser window" :
			String.format("Browse to %s", ABOUT_LOMBOK_URL);
		
		System.out.printf("About lombok v%s\n" +
				"Lombok makes java better by providing very spicy additions to the Java programming language," +
				"such as using @Getter to automatically generate a getter method for any field.\n\n%s" +
				" for more information about the lombok project, and how to" +
				"install it into your programming environment. If you are just using javac (the java compiler)," +
				"just use this jar, no further steps needed.", version, nextStep);
	}
}
