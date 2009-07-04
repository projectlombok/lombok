package lombok.installer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import lombok.core.Version;
import lombok.installer.EclipseLocation.InstallException;
import lombok.installer.EclipseLocation.NotAnEclipseException;
import lombok.installer.EclipseLocation.UninstallException;

public class InstallerWindow {
	private static final URI ABOUT_LOMBOK_URL = URI.create("http://projectlombok.org");
	
	private JFrame appWindow;
	
	private JComponent loadingExpl;

	private Component javacArea;
	private Component eclipseArea;
	private Component uninstallArea;
	private Component howIWorkArea;

	private Box uninstallBox;

	private List<EclipseLocation> toUninstall;
	
	public static void main(String[] args) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						new InstallerWindow().show();
					} catch ( HeadlessException e ) {
						printHeadlessInfo();
					}
				}
			});
		} catch ( HeadlessException e ) {
			printHeadlessInfo();
		}
	}
	
	private static void printHeadlessInfo() {
		System.out.printf("About lombok v%s\n" +
				"Lombok makes java better by providing very spicy additions to the Java programming language," +
				"such as using @Getter to automatically generate a getter method for any field.\n\n" +
				"Browse to %s for more information. To install lombok on eclipse, re-run this jar file on a " +
				"graphical computer system - this message is being shown because your terminal is not graphics capable." +
				"If you are just using 'javac' or a tool that calls on javac, no installation is neccessary; just " +
				"make sure lombok.jar is in the classpath when you compile. Example:\n\n" +
				"   java -cp lombok.jar MyCode.java", Version.getVersion(), ABOUT_LOMBOK_URL);
	}
	
	public InstallerWindow() {
		appWindow = new JFrame(String.format("Project Lombok v%s - Installer", Version.getVersion()));
		
		appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		appWindow.setResizable(false);
		
		try {
			javacArea = buildJavacArea();
			eclipseArea = buildEclipseArea();
			uninstallArea = buildUninstallArea();
			uninstallArea.setVisible(false);
			howIWorkArea = buildHowIWorkArea();
			howIWorkArea.setVisible(false);
			buildChrome(appWindow.getContentPane());
			appWindow.pack();
		} catch ( Throwable t ) {
			handleException(t);
		}
	}
	
	private void handleException(final Throwable t) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				JOptionPane.showMessageDialog(appWindow, "There was a problem during the installation process:\n" + t, "Uh Oh!", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}
	
	private Component buildHowIWorkArea() {
		JPanel container = new JPanel();
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		
		container.add(new JLabel(HOW_I_WORK_TITLE), constraints);
		
		constraints.gridy = 1;
		constraints.insets = new Insets(8, 0, 0, 16);
		container.add(new JLabel(HOW_I_WORK_EXPLANATION), constraints);
		
		Box buttonBar = Box.createHorizontalBox();
		JButton backButton = new JButton("Okay - Good to know!");
		buttonBar.add(Box.createHorizontalGlue());
		buttonBar.add(backButton);
		
		backButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				howIWorkArea.setVisible(false);
				javacArea.setVisible(true);
				eclipseArea.setVisible(true);
				appWindow.pack();
			}
		});
		
		constraints.gridy = 2;
		container.add(buttonBar, constraints);
		
		return container;
	}
	
	private Component buildUninstallArea() {
		JPanel container = new JPanel();
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		
		container.add(new JLabel(UNINSTALL_TITLE), constraints);
		
		constraints.gridy = 1;
		constraints.insets = new Insets(8, 0, 0, 16);
		container.add(new JLabel(UNINSTALL_EXPLANATION), constraints);
		
		uninstallBox = Box.createVerticalBox();
		constraints.gridy = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		container.add(uninstallBox, constraints);
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridy = 3;
		container.add(new JLabel("Are you sure?"), constraints);
		
		Box buttonBar = Box.createHorizontalBox();
		JButton noButton = new JButton("No - Don't uninstall");
		buttonBar.add(noButton);
		buttonBar.add(Box.createHorizontalGlue());
		JButton yesButton = new JButton("Yes - uninstall Lombok");
		buttonBar.add(yesButton);
		
		noButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				uninstallArea.setVisible(false);
				javacArea.setVisible(true);
				eclipseArea.setVisible(true);
				appWindow.pack();
			}
		});
		
		yesButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				doUninstall();
			}
		});
		
		constraints.gridy = 4;
		container.add(buttonBar, constraints);
		
		return container;
	}
	
	private Component buildJavacArea() {
		JPanel container = new JPanel();
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		
		container.add(new JLabel(JAVAC_TITLE), constraints);
		
		constraints.gridy = 1;
		constraints.insets = new Insets(8, 0, 0, 16);
		container.add(new JLabel(JAVAC_EXPLANATION), constraints);
		
		JLabel example = new JLabel(JAVAC_EXAMPLE);
		
		constraints.gridy = 2;
		constraints.insets = new Insets(8, 0, 0, 16);
		container.add(example, constraints);
		return container;
	}
	
	private Component buildEclipseArea() throws IOException {
		//   "Or:" [I'll tell you where eclipse is] [Tell me how to install lombok manually]
		
		//Mode 2 (manual):
		//   Replace the entirety of the content (javac+eclipse) with an explanation about what to do:
		//      - copy lombok.jar to your eclipse directory.
		//      - jar xvf lombok.jar lombok.eclipse.agent.jar
		//      - edit eclipse.ini with:
		//          -javaagent:../../../lombok.eclipse.agent.jar
		//          -Xbootclasspath/a:../../../lombok.eclipse.agent.jar:../../../lombok.jar
		
		//Mode 3 (let me choose):
		//   pop up a file chooser. Make sure we don't care what you pick - eclipse.ini, eclipse.exe, eclipse.app, or dir.
		//     empty the list, remove the spinner and the [let me find eclipse on my own] button, and put the chosen
		//     eclipse in the list.
		
		JPanel container = new JPanel();
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		
		container.add(new JLabel(ECLIPSE_TITLE), constraints);
		
		constraints.gridy = 1;
		constraints.insets = new Insets(8, 0, 0, 16);
		container.add(new JLabel(ECLIPSE_EXPLANATION), constraints);
		
		constraints.gridy = 2;
		loadingExpl = Box.createHorizontalBox();
		loadingExpl.add(new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/loading.gif"))));
		loadingExpl.add(new JLabel(ECLIPSE_LOADING_EXPLANATION));
		container.add(loadingExpl, constraints);
		
		constraints.weightx = 1.0;
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		eclipsesList = new EclipsesList();
		
		JScrollPane eclipsesListScroll = new JScrollPane(eclipsesList);
		eclipsesListScroll.setBackground(Color.WHITE);
		container.add(eclipsesListScroll, constraints);
		
		Thread findEclipsesThread = new Thread() {
			@Override public void run() {
				try {
					final List<String> eclipses = EclipseFinder.findEclipses();
					final List<EclipseLocation> locations = new ArrayList<EclipseLocation>();
					final List<NotAnEclipseException> problems = new ArrayList<NotAnEclipseException>();
					
					for ( String eclipse : eclipses ) try {
						locations.add(new EclipseLocation(eclipse));
					} catch ( NotAnEclipseException e ) {
						problems.add(e);
					}
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override public void run() {
							for ( EclipseLocation location : locations ) {
								try {
									eclipsesList.addEclipse(location);
								} catch ( Throwable t ) {
									handleException(t);
								}
							}
							
							for ( NotAnEclipseException problem : problems ) {
								problem.showDialog(appWindow);
							}
							
							loadingExpl.setVisible(false);
						}
					});
				} catch ( Throwable t ) {
					handleException(t);
				}
			}
		};
		
		findEclipsesThread.start();
		
		Box buttonBar = Box.createHorizontalBox();
		JButton specifyEclipseLocationButton = new JButton("Specify eclipse location...");
		buttonBar.add(specifyEclipseLocationButton);
		specifyEclipseLocationButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser();
				
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileFilter() {
					private final String name = EclipseFinder.getEclipseExecutableName();
					@Override public boolean accept(File f) {
						if ( f.getName().equalsIgnoreCase(name) ) return true;
						if ( f.isDirectory() ) return true;
						
						return false;
					}
					
					@Override public String getDescription() {
						return "Eclipse Installation";
					}
				});
				
				switch ( chooser.showDialog(appWindow, "Select") ) {
				case JFileChooser.APPROVE_OPTION:
					try {
						try {
							eclipsesList.addEclipse(new EclipseLocation(chooser.getSelectedFile().getAbsolutePath()));
						} catch ( NotAnEclipseException e ) {
							e.showDialog(appWindow);
						}
					} catch ( Throwable t ) {
						handleException(t);
					}
				}
			}
		});
		buttonBar.add(Box.createHorizontalGlue());
		installButton = new JButton("Install / Update");
		buttonBar.add(installButton);
		
		installButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				List<EclipseLocation> locationsToInstall = new ArrayList<EclipseLocation>(eclipsesList.getSelectedEclipses());
				if ( locationsToInstall.isEmpty() ) {
					JOptionPane.showMessageDialog(appWindow, "You haven't selected any eclipse installations!.", "No Selection", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				install(locationsToInstall);
			}
		});
		
		constraints.gridy = 4;
		constraints.weightx = 0;
		container.add(buttonBar, constraints);
		
		constraints.gridy = 5;
		constraints.fill = GridBagConstraints.NONE;
		uninstallButton = new JHyperLink("Uninstall lombok from selected eclipse installations.");
		uninstallButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				List<EclipseLocation> locationsToUninstall = new ArrayList<EclipseLocation>();
				for ( EclipseLocation location : eclipsesList.getSelectedEclipses() ) {
					if ( location.hasLombok() ) locationsToUninstall.add(location);
				}
				
				if ( locationsToUninstall.isEmpty() ) {
					JOptionPane.showMessageDialog(appWindow, "You haven't selected any eclipse installations that have been lombok-enabled.", "No Selection", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				
				uninstall(locationsToUninstall);
			}
		});
		container.add(uninstallButton, constraints);
		
		constraints.gridy = 6;
		JHyperLink showMe = new JHyperLink("Show me what this installer will do to my eclipse installation.");
		container.add(showMe, constraints);
		showMe.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				showWhatIDo();
			}
		});
		
		return container;
	}
	
	private void showWhatIDo() {
		javacArea.setVisible(false);
		eclipseArea.setVisible(false);
		howIWorkArea.setVisible(true);
		appWindow.pack();
	}
	
	private void uninstall(List<EclipseLocation> locations) {
		javacArea.setVisible(false);
		eclipseArea.setVisible(false);
		
		uninstallBox.removeAll();
		uninstallBox.add(Box.createRigidArea(new Dimension(1, 16)));
		for ( EclipseLocation location : locations ) {
			JLabel label = new JLabel(location.getPath());
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			uninstallBox.add(label);
		}
		uninstallBox.add(Box.createRigidArea(new Dimension(1, 16)));
		
		toUninstall = locations;
		uninstallArea.setVisible(true);
		appWindow.pack();
	}
	
	private void install(final List<EclipseLocation> toInstall) {
		JPanel spinner = new JPanel();
		spinner.setOpaque(true);
		spinner.setLayout(new FlowLayout());
		spinner.add(new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/loading.gif"))));
		appWindow.setContentPane(spinner);
		
		final AtomicReference<Boolean> success = new AtomicReference<Boolean>(true);
		
		new Thread() {
			@Override public void run() {
				for ( EclipseLocation loc : toInstall ) {
					try {
						loc.install();
					} catch ( final InstallException e ) {
						success.set(false);
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override public void run() {
									JOptionPane.showMessageDialog(appWindow,
											e.getMessage(), "Install Problem", JOptionPane.ERROR_MESSAGE);
								}
							});
						} catch ( Exception e2 ) {
							//Shouldn't happen.
							throw new RuntimeException(e2);
						}
					}
				}
				
				if ( success.get() ) SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						JOptionPane.showMessageDialog(appWindow, "Lombok has been installed on the selected eclipse installations.", "Install successful", JOptionPane.INFORMATION_MESSAGE);
						appWindow.setVisible(false);
						System.exit(0);
					}
				});
			}
		}.start();
	}
	
	private void doUninstall() {
		JPanel spinner = new JPanel();
		spinner.setOpaque(true);
		spinner.setLayout(new FlowLayout());
		spinner.add(new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/loading.gif"))));
		
		appWindow.setContentPane(spinner);
		
		final AtomicReference<Boolean> success = new AtomicReference<Boolean>(true);
		new Thread() {
			@Override public void run() {
				for ( EclipseLocation loc : toUninstall ) {
					try {
						loc.uninstall();
					} catch ( final UninstallException e ) {
						success.set(false);
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override public void run() {
									JOptionPane.showMessageDialog(appWindow,
											e.getMessage(), "Uninstall Problem", JOptionPane.ERROR_MESSAGE);
								}
							});
						} catch ( Exception e2 ) {
							//Shouldn't happen.
							throw new RuntimeException(e2);
						}
					}
				}
				
				if ( success.get() ) SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						JOptionPane.showMessageDialog(appWindow, "Lombok has been removed from the selected eclipse installations.", "Uninstall successful", JOptionPane.INFORMATION_MESSAGE);
						appWindow.setVisible(false);
						System.exit(0);
					}
				});
			}
		}.start();
	}
	
	private EclipsesList eclipsesList = new EclipsesList();
	
	private static class JHyperLink extends JButton {
		private static final long serialVersionUID = 1L;
		
		public JHyperLink(String text) {
			super();
			setFont(getFont().deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, 1)));
			setText(text);
			setBorder(null);
			setContentAreaFilled(false);
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setMargin(new Insets(0, 0, 0, 0));
		}
	}
	
	void selectedLomboksChanged(List<EclipseLocation> selectedEclipses) {
		boolean uninstallAvailable = false;
		boolean installAvailable = false;
		for ( EclipseLocation loc : selectedEclipses ) {
			if ( loc.hasLombok() ) uninstallAvailable = true;
			installAvailable = true;
		}
		
		uninstallButton.setVisible(uninstallAvailable);
		installButton.setEnabled(installAvailable);
	}
	
	private class EclipsesList extends JPanel implements Scrollable {
		private static final long serialVersionUID = 1L;
		
		List<EclipseLocation> locations = new ArrayList<EclipseLocation>();
		
		EclipsesList() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(Color.WHITE);
		}
		
		List<EclipseLocation> getSelectedEclipses() {
			List<EclipseLocation> list = new ArrayList<EclipseLocation>();
			for ( EclipseLocation loc : locations ) if ( loc.selected ) list.add(loc);
			return list;
		}
		
		void fireSelectionChange() {
			selectedLomboksChanged(getSelectedEclipses());
		}
		
		void addEclipse(final EclipseLocation location) {
			if ( locations.contains(location) ) return;
			Box box = Box.createHorizontalBox();
			box.setBackground(Color.WHITE);
			final JCheckBox checkbox = new JCheckBox(location.getPath());
			box.add(checkbox);
			checkbox.setSelected(true);
			checkbox.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					location.selected = checkbox.isSelected();
					fireSelectionChange();
				}
			});
			
			if ( location.hasLombok() ) {
				box.add(new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/lombokIcon.png"))));
			}
			box.add(Box.createHorizontalGlue());
			locations.add(location);
			add(box);
			getParent().doLayout();
			fireSelectionChange();
		}
		
		@Override public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(1, 100);
		}
		
		@Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 12;
		}
		
		@Override public boolean getScrollableTracksViewportHeight() {
			return false;
		}
		
		@Override public boolean getScrollableTracksViewportWidth() {
			return true;
		}
		
		@Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 1;
		}
	};
	
	private void buildChrome(Container appWindowContainer) {
		JLabel leftGraphic = new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/lombok.png")));
		JLabel topGraphic = new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/lombokText.png")));
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		appWindowContainer.setLayout(new GridBagLayout());
		
		constraints.gridheight = 3;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(8, 8, 8, 8);
		appWindowContainer.add(leftGraphic,constraints);
		
		constraints.gridx++;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.NONE;
		constraints.ipadx = 40;
		constraints.ipady = 64;
		appWindowContainer.add(topGraphic, constraints);
		
		constraints.gridy++;
		constraints.ipadx = 16;
		constraints.ipady = 16;
		appWindowContainer.add(javacArea, constraints);
		
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		appWindowContainer.add(eclipseArea, constraints);
		
		appWindowContainer.add(uninstallArea, constraints);
		
		appWindowContainer.add(howIWorkArea, constraints);
		
		constraints.gridy++;
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.ipadx = 0;
		constraints.ipady = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.SOUTHEAST;
		constraints.insets = new Insets(0, 16, 8, 8);
		Box buttonBar = Box.createHorizontalBox();
		JButton quitButton = new JButton("Quit Installer");
		quitButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				appWindow.setVisible(false);
				System.exit(0);
			}
		});
		final JHyperLink hyperlink = new JHyperLink(ABOUT_LOMBOK_URL.toString());
		hyperlink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				hyperlink.setForeground(new Color(85, 145, 90));
				try {
					//java.awt.Desktop doesn't exist in 1.5.
					Object desktop = Class.forName("java.awt.Desktop").getMethod("getDesktop").invoke(null);
					Class.forName("java.awt.Desktop").getMethod("browse", URI.class).invoke(desktop, ABOUT_LOMBOK_URL);
				} catch ( Exception e ) {
					String os = System.getProperty("os.name").toLowerCase();
					Runtime rt = Runtime.getRuntime();
					try {
						if ( os.indexOf( "win" ) > -1 ) {
							String[] cmd = new String[4];
							cmd[0] = "cmd.exe";
							cmd[1] = "/C";
							cmd[2] = "start";
							cmd[3] = ABOUT_LOMBOK_URL.toString();
							rt.exec(cmd);
						} else if ( os.indexOf( "mac" ) >= 0 ) {
							rt.exec( "open " + ABOUT_LOMBOK_URL.toString());
						} else {
							rt.exec("firefox " + ABOUT_LOMBOK_URL.toString());
						}
					} catch ( Exception e2 ) {
						JOptionPane.showMessageDialog(appWindow,
								"Well, this is embarrassing. I don't know how to open a webbrowser.\n" +
								"I guess you'll have to open it. Browse to:\n" +
								"http://projectlombok.org for more information about Lombok.",
								"I'm embarrassed", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});
		buttonBar.add(hyperlink);
		buttonBar.add(Box.createRigidArea(new Dimension(16, 1)));
		buttonBar.add(new JLabel("<html><font size=\"-1\">v" + Version.getVersion() + "</font></html>"));
		
		buttonBar.add(Box.createHorizontalGlue());
		buttonBar.add(quitButton);
		appWindow.add(buttonBar, constraints);
	}
	
	public void show() {
		appWindow.setVisible(true);
	}
	
	private static final String ECLIPSE_TITLE =
		"<html><font size=\"+1\"><b><i>Eclipse</i></b></font></html>";
	
	private static final String ECLIPSE_EXPLANATION =
		"<html>Lombok can update your eclipse to fully support all Lombok features.<br>" +
		"Select eclipse installations below and hit 'Install/Update'.</html>";
	
	private static final String ECLIPSE_LOADING_EXPLANATION =
		"Scanning your drives for eclipse installations...";
	
	private static final String JAVAC_TITLE =
		"<html><font size=\"+1\"><b><i>Javac</i></b></font> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; (and tools that invoke javac such as <i>ant</i> and <i>maven</i>)</html>";
	
	private static final String JAVAC_EXPLANATION =
		"<html>Lombok works 'out of the box' with javac.<br>Just make sure the lombok.jar is in your classpath when you compile.";
	
	private static final String JAVAC_EXAMPLE =
		"<html>Example: <code>javac -cp lombok.jar MyCode.java</code></html>";
	
	private static final String UNINSTALL_TITLE =
		"<html><font size=\"+1\"><b><i>Uninstall</i></b></font></html>";
	
	private static final String UNINSTALL_EXPLANATION =
		"<html>Uninstall Lombok from the following Eclipse Installations?</html>";
	
	private static final String HOW_I_WORK_TITLE =
		"<html><font size=\"+1\"><b><i>What this installer does</i></b></font></html>";
	
	private static final String HOW_I_WORK_EXPLANATION =
		"<html><ol>" +
		"<li>First, I copy myself (lombok.jar) to your eclipse install directory.</li>" +
		"<li>Then, I unpack lombok.eclipse.agent.jar like so:<br>" +
		"<pre>jar xvf lombok.jar lombok.eclipse.agent.jar</pre></li>" +
		"<li>Then, I edit the eclipse.ini file to add the following two entries:<br>" +
		"<pre>-Xbootclasspath/a:lombok.jar:lombok.eclipse.agent.jar<br>" +
		"-javaagent:lombok.jar</pre></li></ol>" +
		"<br>" +
		"That's all there is to it. Note that on Mac OS X, eclipse.ini is hidden in<br>" +
		"<code>Eclipse.app/Contents/MacOS</code> so that's where I place the jar files.</html>";
	
	private JHyperLink uninstallButton;

	private JButton installButton;
}
