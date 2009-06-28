package lombok.core;

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
			//java.awt.Desktop doesn't exist in 1.5, and for IDE's sakes, we may want to work in java1.5 someday, so...
			Object desktop = Class.forName("java.awt.Desktop").getMethod("getDesktop").invoke(null);
			Class.forName("java.awt.Desktop").getMethod("browse", URI.class).invoke(desktop, ABOUT_LOMBOK_URL);
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
