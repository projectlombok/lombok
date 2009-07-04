package lombok.installer;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Lombok;

public class EclipseFinder {
	public static File findOurJar() {
		try {
			URI uri = EclipseFinder.class.getResource("/" + EclipseFinder.class.getName().replace('.', '/') + ".class").toURI();
			Pattern p = Pattern.compile("^jar:file:([^\\!]+)\\!.*\\.class$");
			System.out.println(uri.toString());
			Matcher m = p.matcher(uri.toString());
			if ( !m.matches() ) return new File("lombok.jar");
			String rawUri = m.group(1);
			return new File(URLDecoder.decode(rawUri, Charset.defaultCharset().name()));
		} catch ( Exception e ) {
			throw Lombok.sneakyThrow(e);
		}
	}
	
	public static List<String> getDrivesOnWindows() throws IOException {
		ProcessBuilder builder = new ProcessBuilder("c:\\windows\\system32\\fsutil.exe", "fsinfo", "drives");
		builder.redirectErrorStream(true);
		Process process = builder.start();
		InputStream in = process.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
		
		List<String> drives = new ArrayList<String>();
		
		String line;
		while ( (line = br.readLine()) != null ) {
			if ( line.startsWith("Drives: ") ) {
				line = line.substring(8);
				for ( String driveLetter : line.split("\\:\\\\\\s*") ) drives.add(driveLetter.trim());
			}
		}
		
		Iterator<String> it = drives.iterator();
		while ( it.hasNext() ) {
			if ( !isLocalDriveOnWindows(it.next()) ) it.remove();
		}
		
		return drives;
	}
	
	public static boolean isLocalDriveOnWindows(String driveLetter) {
		if ( driveLetter == null || driveLetter.length() == 0 ) return false;
		try {
			ProcessBuilder builder = new ProcessBuilder("c:\\windows\\system32\\fsutil.exe", "fsinfo", "drivetype", driveLetter + ":");
			builder.redirectErrorStream(true);
			Process process = builder.start();
			InputStream in = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
			
			String line;
			while ( (line = br.readLine()) != null ) {
				if ( line.substring(5).equalsIgnoreCase("Fixed Drive") ) return true;
			}
			
			return false;
		} catch ( Exception e ) {
			return false;
		}
	}
	
	public static List<String> findEclipseOnWindows() {
		List<String> eclipses = new ArrayList<String>();
		List<String> driveLetters = asList("C");
		
		try {
			driveLetters = getDrivesOnWindows();
		} catch ( IOException ignore ) {}
		
		for ( String letter : driveLetters ) {
			File f = new File(letter + ":\\");
			for ( File dir : f.listFiles() ) {
				if ( !dir.isDirectory() ) continue;
				if ( dir.getName().toLowerCase().contains("eclipse") ) {
					String eclipseLocation = findEclipseOnWindows1(dir);
					if ( eclipseLocation != null ) eclipses.add(eclipseLocation);
				}
				if ( dir.getName().toLowerCase().contains("program files") ) {
					for ( File dir2 : dir.listFiles() ) {
						if ( !dir2.isDirectory() ) continue;
						if ( dir.getName().toLowerCase().contains("eclipse") ) {
							String eclipseLocation = findEclipseOnWindows1(dir);
							if ( eclipseLocation != null ) eclipses.add(eclipseLocation);
						}
					}
				}
			}
		}
		
		return eclipses;
	}
	
	private static String findEclipseOnWindows1(File dir) {
		if ( new File(dir, "eclipse.exe").isFile() ) return dir.toString();
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(findEclipses());
	}
	
	public static List<String> findEclipses() {
		String prop = System.getProperty("os.name", "");
		if ( prop.equalsIgnoreCase("Mac OS X") ) return findEclipseOnMac();
		if ( prop.equalsIgnoreCase("Windows XP") ) return findEclipseOnWindows();
		
		return null;
	}
	
	public static String getEclipseExecutableName() {
		String prop = System.getProperty("os.name", "");
		if ( prop.equalsIgnoreCase("Mac OS X") ) return "Eclipse.app";
		if ( prop.equalsIgnoreCase("Windows XP") ) return "eclipse.exe";
		
		return "eclipse";
	}
	
	public static List<String> findEclipseOnMac() {
		List<String> eclipses = new ArrayList<String>();
		for ( File dir : new File("/Applications").listFiles() ) {
			if ( dir.getName().toLowerCase().contains("eclipse") ) {
				if ( new File(dir, "Eclipse.app").exists() ) eclipses.add(dir.toString());
			}
		}
		return eclipses;
	}
}
