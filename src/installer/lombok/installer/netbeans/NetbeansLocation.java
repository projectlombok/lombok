/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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
package lombok.installer.netbeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.IdeFinder;
import lombok.installer.IdeLocation;
import lombok.installer.InstallException;
import lombok.installer.Installer;
import lombok.installer.UninstallException;

public class NetbeansLocation extends IdeLocation {
	private final String name;
	private final File netbeansConfPath;
	private volatile boolean hasLombok;
	
	private static final String OS_NEWLINE = IdeFinder.getOS().getLineEnding();
	
	NetbeansLocation(String nameOfLocation, File pathToNetbeansConf) throws CorruptedIdeLocationException {
		this.name = nameOfLocation;
		this.netbeansConfPath = pathToNetbeansConf;
		try {
			this.hasLombok = checkForLombok(netbeansConfPath);
		} catch (IOException e) {
			throw new CorruptedIdeLocationException(
					"I can't read the configuration file of the Netbeans installed at " + name + "\n" +
					"You may need to run this installer with root privileges if you want to modify that Netbeans.", "netbeans", e);
		}
	}
	
	@Override public int hashCode() {
		return netbeansConfPath.hashCode();
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof NetbeansLocation)) return false;
		return ((NetbeansLocation)o).netbeansConfPath.equals(netbeansConfPath);
	}
	
	/**
	 * Returns the name of this location; generally the path to the netbeans executable.
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @return true if the Netbeans installation has been instrumented with lombok.
	 */
	@Override
	public boolean hasLombok() {
		return hasLombok;
	}
	
	private final String ID_CHARS = "(?:\\\\.|[^\"\\\\])*";
	private final Pattern JAVA_AGENT_LINE_MATCHER = Pattern.compile(
			"^\\s*netbeans_default_options\\s*=\\s*\"\\s*" + ID_CHARS + "(?<=[ \"])(-J-javaagent:\\\\\".*lombok.*\\.jar\\\\\")(?=[ \"])" + ID_CHARS +"\\s*\"\\s*(?:#.*)?$", Pattern.CASE_INSENSITIVE);
	
	private final Pattern OPTIONS_LINE_MATCHER = Pattern.compile(
			"^\\s*netbeans_default_options\\s*=\\s*\"\\s*" + ID_CHARS + "\\s*(\")\\s*(?:#.*)?$", Pattern.CASE_INSENSITIVE);
	
	private boolean checkForLombok(File iniFile) throws IOException {
		if (!iniFile.exists()) return false;
		FileInputStream fis = new FileInputStream(iniFile);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			while ((line = br.readLine()) != null) {
				if (JAVA_AGENT_LINE_MATCHER.matcher(line.trim()).matches()) return true;
			}
			
			return false;
		} finally {
			fis.close();
		}
	}
	
	/**
	 * Uninstalls lombok from this location.
	 * It's a no-op if lombok wasn't there in the first place,
	 * and it will remove a half-succeeded lombok installation as well.
	 * 
	 * @throws UninstallException
	 *             If there's an obvious I/O problem that is preventing
	 *             installation. bugs in the uninstall code will probably throw
	 *             other exceptions; this is intentional.
	 */
	@Override 
	public void uninstall() throws UninstallException {
		final List<File> lombokJarsForWhichCantDeleteSelf = new ArrayList<File>();
		File dir = netbeansConfPath.getParentFile();
		
		StringBuilder newContents = new StringBuilder();
		if (netbeansConfPath.exists()) {
			try {
				FileInputStream fis = new FileInputStream(netbeansConfPath);
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					String line;
					while ((line = br.readLine()) != null) {
						Matcher m = JAVA_AGENT_LINE_MATCHER.matcher(line);
						if (m.matches()) {
							newContents.append(line.substring(0, m.start(1)) + line.substring(m.end(1)));
						} else {
							newContents.append(line);
						}
						newContents.append(OS_NEWLINE);
					}
				} finally {
					fis.close();
				}
				
				FileOutputStream fos = new FileOutputStream(netbeansConfPath);
				try {
					fos.write(newContents.toString().getBytes());
				} finally {
					fos.close();
				}
			} catch (IOException e) {
				throw new UninstallException("Cannot uninstall lombok from " + name + generateWriteErrorMessage(), e);
			}
		}
		
		File lombokJar = new File(dir, "lombok.jar");
		if (lombokJar.exists()) {
			if (IdeFinder.getOS() == IdeFinder.OS.WINDOWS && Installer.isSelf(lombokJar.getAbsolutePath())) {
				lombokJarsForWhichCantDeleteSelf.add(lombokJar);
			} else {
				throw new UninstallException(
					"Can't delete " + lombokJar.getAbsolutePath() + generateWriteErrorMessage(), null);
			}
		}
		
		if (!lombokJarsForWhichCantDeleteSelf.isEmpty()) {
			throw new UninstallException(true,
					"lombok.jar cannot delete itself on windows.\nHowever, lombok has been uncoupled from your netbeans.\n" +
					"You can safely delete this jar file. You can find it at:\n" +
					lombokJarsForWhichCantDeleteSelf.get(0).getAbsolutePath(), null);
		}
	}
	
	private static String generateWriteErrorMessage() {
		String osSpecificError;
		
		switch (IdeFinder.getOS()) {
		default:
		case MAC_OS_X:
		case UNIX:
			osSpecificError = ":\nStart terminal, go to the directory with lombok.jar, and run: sudo java -jar lombok.jar";
			break;
		case WINDOWS:
			osSpecificError = ":\nStart a new cmd (dos box) with admin privileges, go to the directory with lombok.jar, and run: java -jar lombok.jar";
			break;
		}
		
		return ", probably because this installer does not have the access rights.\n" +
		"Try re-running the installer with administrative privileges" + osSpecificError;
	}
	
	/**
	 * Install lombok into the Netbeans at this location.
	 * If lombok is already there, it is overwritten neatly (upgrade mode).
	 * 
	 * @throws InstallException
	 *             If there's an obvious I/O problem that is preventing
	 *             installation. bugs in the install code will probably throw
	 *             other exceptions; this is intentional.
	 */
	@Override
	public String install() throws InstallException {
		boolean installSucceeded = false;
		StringBuilder newContents = new StringBuilder();
		
		File lombokJar = new File(netbeansConfPath.getParentFile(), "lombok.jar");
		
		/* No need to copy lombok.jar to itself, obviously. On windows this would generate an error so we check for this. */
		if (!Installer.isSelf(lombokJar.getAbsolutePath())) {
			File ourJar = findOurJar();
			byte[] b = new byte[524288];
			boolean readSucceeded = true;
			try {
				FileOutputStream out = new FileOutputStream(lombokJar);
				try {
					readSucceeded = false;
					InputStream in = new FileInputStream(ourJar);
					try {
						while (true) {
							int r = in.read(b);
							if (r == -1) break;
							if (r > 0) readSucceeded = true;
							out.write(b, 0, r);
						}
					} finally {
						in.close();
					}
				} finally {
					out.close();
				}
			} catch (IOException e) {
				try {
					lombokJar.delete();
				} catch (Throwable ignore) { /* Nothing we can do about that. */ }
				if (!readSucceeded) throw new InstallException(
						"I can't read my own jar file. I think you've found a bug in this installer!\nI suggest you restart it " +
						"and use the 'what do I do' link, to manually install lombok. Also, tell us about this at:\n" +
						"http://groups.google.com/group/project-lombok - Thanks!", e);
				throw new InstallException("I can't write to your Netbeans directory at " + name + generateWriteErrorMessage(), e);
			}
		}
		
		try {
			FileInputStream fis = new FileInputStream(netbeansConfPath);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String line;
				while ((line = br.readLine()) != null) {
					Matcher m = JAVA_AGENT_LINE_MATCHER.matcher(line);
					if (m.matches()) {
						newContents.append(line.substring(0, m.start(1)));
						newContents.append("-J-javaagent:\\\"" + canonical(lombokJar) + "\\\"");
						newContents.append(line.substring(m.end(1)));
						newContents.append(OS_NEWLINE);
						continue;
					}
					
					m = OPTIONS_LINE_MATCHER.matcher(line);
					if (m.matches()) {
						newContents.append(line.substring(0, m.start(1)));
						newContents.append(" ").append("-J-javaagent:\\\"" + canonical(lombokJar) +"\\\"\"");
						newContents.append(line.substring(m.end(1))).append(OS_NEWLINE);
						continue;
					}
					
					newContents.append(line).append(OS_NEWLINE);
				}
			} finally {
				fis.close();
			}
			
			FileOutputStream fos = new FileOutputStream(netbeansConfPath);
			try {
				fos.write(newContents.toString().getBytes());
			} finally {
				fos.close();
			}
			installSucceeded = true;
		} catch (IOException e) {
			throw new InstallException("Cannot install lombok at " + name + generateWriteErrorMessage(), e);
		} finally {
			if (!installSucceeded) try {
				lombokJar.delete();
			} catch (Throwable ignore) {}
		}
		
		if (!installSucceeded) {
			throw new InstallException("I can't find the netbeans.conf file. Is this a real Netbeans installation?", null);
		}
		
		return "If you start netbeans with custom parameters, you'll need to add:<br>" +
				"<code>-J-javaagent:\\\"" + canonical(lombokJar) + "\\\"</code><br>" +
				"as parameter as well.";
	}
	
	@Override public URL getIdeIcon() {
		return NetbeansLocation.class.getResource("netbeans.png");
	}
}
