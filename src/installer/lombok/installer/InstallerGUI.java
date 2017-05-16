/*
 * Copyright (C) 2009-2017 The Project Lombok Authors.
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
package lombok.installer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

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
import javax.swing.JTextPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLDocument;

import lombok.core.Version;
import lombok.installer.OsUtils.OS;

/**
 * The lombok GUI installer.
 * Uses swing to show a simple GUI that can add and remove the java agent to IDE installations.
 * Also offers info on what this installer does in case people want to instrument their IDE manually.
 */
public class InstallerGUI {
	private static final int INSTALLER_WINDOW_WIDTH = 662;
	static final AtomicReference<Integer> exitMarker = new AtomicReference<Integer>();
	
	private JFrame appWindow;
	
	private JComponent loadingExpl;

	private Component javacArea;
	private Component ideArea;
	private Component uninstallArea;
	private Component howIWorkArea;
	private Component successArea;

	private Box uninstallBox;
	private JHyperLink uninstallButton;
	private JLabel uninstallPlaceholder;
	private JButton installButton;
	
	private List<IdeLocation> toUninstall;
	private final Set<String> installSpecificMessages = new LinkedHashSet<String>();
	
	/**
	 * Creates a new installer that starts out invisible.
	 * Call the {@link #show()} method on a freshly created installer to render it.
	 */
	public InstallerGUI() {
		appWindow = new JFrame(String.format("Project Lombok v%s - Installer", Version.getVersion()));
		
		appWindow.setLocationByPlatform(true);
		appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		appWindow.setResizable(false);
		appWindow.setIconImage(Toolkit.getDefaultToolkit().getImage(Installer.class.getResource("lombokIcon.png")));
		
		try {
			javacArea = buildJavacArea();
			ideArea = buildIdeArea();
			uninstallArea = buildUninstallArea();
			uninstallArea.setVisible(false);
			howIWorkArea = buildHowIWorkArea();
			howIWorkArea.setVisible(false);
			successArea = buildSuccessArea();
			successArea.setVisible(false);
			buildChrome(appWindow.getContentPane());
			appWindow.pack();
		} catch (Throwable t) {
			handleException(t);
		}
	}
	
	private void handleException(final Throwable t) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				JOptionPane.showMessageDialog(appWindow, "There was a problem during the installation process:\n" + t, "Uh Oh!", JOptionPane.ERROR_MESSAGE);
				t.printStackTrace();
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
		container.add(new JLabel(String.format(HOW_I_WORK_EXPLANATION, File.separator)), constraints);
		
		Box buttonBar = Box.createHorizontalBox();
		JButton backButton = new JButton("Okay - Good to know!");
		buttonBar.add(Box.createHorizontalGlue());
		buttonBar.add(backButton);
		
		backButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				howIWorkArea.setVisible(false);
				javacArea.setVisible(true);
				ideArea.setVisible(true);
				successArea.setVisible(false);
				appWindow.pack();
			}
		});
		
		constraints.gridy = 2;
		container.add(buttonBar, constraints);
		
		container.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH, 415));
		container.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH, 415));
		return container;
	}
	
	private void showSuccess(String installSpecific) {
		successExplanation.setText(SUCCESS_EXPLANATION.replace("%%%", installSpecific));
		howIWorkArea.setVisible(false);
		javacArea.setVisible(false);
		ideArea.setVisible(false);
		successArea.setVisible(true);
		appWindow.pack();
	}
	
	private JLabel successExplanation;
	
	private Component buildSuccessArea() {
		JPanel container = new JPanel();
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		
		JLabel title;
		container.add(title = new JLabel(SUCCESS_TITLE), constraints);
		title.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH - 82, 20));
		title.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH - 82, 20));
		
		constraints.gridy = 1;
		constraints.insets = new Insets(8, 0, 0, 16);
		container.add(successExplanation = new JLabel(SUCCESS_EXPLANATION), constraints);
		successExplanation.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH - 82, 175));
		successExplanation.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH - 82, 175));
		
		constraints.gridy++;
		constraints.fill = GridBagConstraints.BOTH;
		
		JTextPane notes = new JTextPane();
		notes.setContentType("text/html");
		notes.setText(readChangeLog());
		notes.setEditable(false);
		notes.setOpaque(false);
		notes.setBorder(null);
		notes.setSelectionStart(0);
		notes.setSelectionEnd(0);
		
		Font font = UIManager.getFont("Label.font");
		String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
		((HTMLDocument) notes.getDocument()).getStyleSheet().addRule(bodyRule);
		JScrollPane scroller = new JScrollPane(notes);
		container.add(scroller, constraints);
		scroller.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH - 82, 200));
		scroller.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH - 82, 200));
		container.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH, 415));
		container.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH, 415));
		return container;
	}
	
	private String readChangeLog() {
		InputStream in = Installer.class.getResourceAsStream("/latestchanges.html");
		try {
			char[] buff = new char[8192];
			StringBuilder contents = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(in, "UTF-8");
			while (true) {
				int read = reader.read(buff);
				if (read == -1) break;
				contents.append(buff, 0, read);
			}
			return "<html>" + contents + "</html>";
		} catch (Exception e) {
			return "No Changelog available";
		}
		finally {
			try {
				in.close();
			} catch (Exception ignore){ /**/}
		}
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
				ideArea.setVisible(true);
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
		
		container.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH, 415));
		container.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH, 415));
		return container;
	}
	
	private Component buildJavacArea() {
		JPanel container = new JPanel();
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(8, 0, 0, 16);
		
		container.add(new JLabel(JAVAC_TITLE), constraints);
		
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		container.add(new JLabel(JAVAC_EXPLANATION), constraints);
		
		JLabel example = new JLabel(JAVAC_EXAMPLE);
		
		constraints.gridy = 2;
		container.add(example, constraints);
		container.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH, 105));
		container.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH, 105));
		return container;
	}
	
	private Component buildIdeArea() {
		JPanel container = new JPanel();
		
		container.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		
		constraints.insets = new Insets(8, 0, 0, 16);
		container.add(new JLabel(IDE_TITLE), constraints);
		
		constraints.gridy = 1;
		container.add(new JLabel(IDE_EXPLANATION), constraints);
		
		constraints.gridy = 2;
		loadingExpl = Box.createHorizontalBox();
		loadingExpl.add(new JLabel(new ImageIcon(Installer.class.getResource("loading.gif"))));
		loadingExpl.add(new JLabel(IDE_LOADING_EXPLANATION));
		container.add(loadingExpl, constraints);
		
		constraints.weightx = 1.0;
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		idesList = new IdesList();
		
		JScrollPane idesListScroll = new JScrollPane(idesList);
		idesListScroll.setBackground(Color.WHITE);
		idesListScroll.getViewport().setBackground(Color.WHITE);
		container.add(idesListScroll, constraints);
		
		Thread findIdesThread = new Thread() {
			@Override public void run() {
				try {
					final List<IdeLocation> locations = new ArrayList<IdeLocation>();
					final List<CorruptedIdeLocationException> problems = new ArrayList<CorruptedIdeLocationException>();
					Installer.autoDiscover(locations, problems);
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override public void run() {
							for (IdeLocation location : locations) {
								try {
									idesList.addLocation(location);
								} catch (Throwable t) {
									handleException(t);
								}
							}
							
							for (CorruptedIdeLocationException problem : problems) {
								problem.showDialog(appWindow);
							}
							
							loadingExpl.setVisible(false);
							
							if (locations.size() + problems.size() == 0) {
								JOptionPane.showMessageDialog(appWindow,
									"I can't find any IDEs on your computer.\n" +
									"If you have IDEs installed on this computer, please use the " +
									"'Specify Location...' button to manually point out the \n" +
									"location of your IDE installation to me. Thanks!",
									"Can't find IDE", JOptionPane.INFORMATION_MESSAGE);
							}
						}
					});
				} catch (Throwable t) {
					handleException(t);
				}
			}
		};
		
		findIdesThread.start();
		
		Box buttonBar = Box.createHorizontalBox();
		JButton specifyIdeLocationButton = new JButton("Specify location...");
		buttonBar.add(specifyIdeLocationButton);
		specifyIdeLocationButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				final List<Pattern> exeNames = Installer.getIdeExecutableNames();
				String file = null;
				
				if (OsUtils.getOS() == OS.MAC_OS_X) {
					FileDialog chooser = new FileDialog(appWindow);
					chooser.setMode(FileDialog.LOAD);
					
					chooser.setFilenameFilter(new FilenameFilter() {
						@Override public boolean accept(File dir, String fileName) {
							for (Pattern exeName : exeNames) if (exeName.matcher(fileName).matches()) return true;
							return false;
						}
					});
					
					chooser.setVisible(true);
					if (chooser.getDirectory() != null && chooser.getFile() != null) {
						file = new File(chooser.getDirectory(), chooser.getFile()).getAbsolutePath();
					}
				} else {
					JFileChooser chooser = new JFileChooser();
					
					chooser.setAcceptAllFileFilterUsed(false);
					chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					chooser.setFileFilter(new FileFilter() {
						@Override public boolean accept(File f) {
							if (f.isDirectory()) return true;
							for (Pattern exeName : exeNames) if (exeName.matcher(f.getName()).matches()) return true;
							
							return false;
						}
						
						@Override public String getDescription() {
							return "IDE Installation";
						}
					});
					
					switch (chooser.showDialog(appWindow, "Select")) {
					case JFileChooser.APPROVE_OPTION:
						file = chooser.getSelectedFile().getAbsolutePath();
					}
				}
				
				if (file != null) {
					try {
						IdeLocation loc = Installer.tryAllProviders(file);
						if (loc != null) idesList.addLocation(loc);
						else JOptionPane.showMessageDialog(appWindow, "I can't find any IDE that lombok supports at location: " + file, "No IDE found", JOptionPane.WARNING_MESSAGE);
					} catch (CorruptedIdeLocationException e) {
						e.showDialog(appWindow);
					} catch (Throwable t) {
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
				List<IdeLocation> locationsToInstall = new ArrayList<IdeLocation>(idesList.getSelectedIdes());
				if (locationsToInstall.isEmpty()) {
					JOptionPane.showMessageDialog(appWindow, "You haven't selected any IDE installations!.", "No Selection", JOptionPane.WARNING_MESSAGE);
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
		JHyperLink showMe = new JHyperLink("Show me what this installer will do to my IDE installation.");
		container.add(showMe, constraints);
		showMe.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				showWhatIDo();
			}
		});
		
		constraints.gridy = 6;
		uninstallButton = new JHyperLink("Uninstall lombok from selected IDE installations.");
		uninstallPlaceholder = new JLabel("<html>&nbsp;</html>");
		uninstallButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				List<IdeLocation> locationsToUninstall = new ArrayList<IdeLocation>();
				for (IdeLocation location : idesList.getSelectedIdes()) {
					if (location.hasLombok()) locationsToUninstall.add(location);
				}
				
				if (locationsToUninstall.isEmpty()) {
					JOptionPane.showMessageDialog(appWindow, "You haven't selected any IDE installations that have been lombok-enabled.", "No Selection", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				
				uninstall(locationsToUninstall);
			}
		});
		container.add(uninstallButton, constraints);
		uninstallPlaceholder.setVisible(false);
		container.add(uninstallPlaceholder, constraints);
		
		container.setPreferredSize(new Dimension(INSTALLER_WINDOW_WIDTH, 296));
		container.setMinimumSize(new Dimension(INSTALLER_WINDOW_WIDTH, 296));
		return container;
	}
	
	private void showWhatIDo() {
		javacArea.setVisible(false);
		ideArea.setVisible(false);
		howIWorkArea.setVisible(true);
		successArea.setVisible(false);
		appWindow.pack();
	}
	
	private void uninstall(List<IdeLocation> locations) {
		javacArea.setVisible(false);
		ideArea.setVisible(false);
		
		uninstallBox.removeAll();
		uninstallBox.add(Box.createRigidArea(new Dimension(1, 16)));
		for (IdeLocation location : locations) {
			JLabel label = new JLabel(location.getName());
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			uninstallBox.add(label);
		}
		uninstallBox.add(Box.createRigidArea(new Dimension(1, 16)));
		
		toUninstall = locations;
		uninstallArea.setVisible(true);
		appWindow.pack();
	}
	
	private void install(final List<IdeLocation> toInstall) {
		JPanel spinner = new JPanel();
		spinner.setOpaque(true);
		spinner.setLayout(new FlowLayout());
		spinner.add(new JLabel(new ImageIcon(Installer.class.getResource("loading.gif"))));
		final Container appWindowContent = appWindow.getContentPane();
		appWindow.setContentPane(spinner);
		
		final AtomicInteger successes = new AtomicInteger();
		final AtomicBoolean failure = new AtomicBoolean();
		
		new Thread() {
			@Override public void run() {
				for (IdeLocation loc : toInstall) {
					try {
						installSpecificMessages.add(loc.install());
						successes.incrementAndGet();
					} catch (final InstallException e) {
						if (e.isWarning()) {
							try {
								SwingUtilities.invokeAndWait(new Runnable() {
									@Override public void run() {
										JOptionPane.showMessageDialog(appWindow,
												e.getMessage(), "Install Problem", JOptionPane.WARNING_MESSAGE);
									}
								});
							} catch (Exception e2) {
								e2.printStackTrace();
								//Shouldn't happen.
								throw new RuntimeException(e2);
							}
						} else {
							failure.set(true);
							try {
								SwingUtilities.invokeAndWait(new Runnable() {
									@Override public void run() {
										JOptionPane.showMessageDialog(appWindow,
												e.getMessage(), "Install Problem", JOptionPane.ERROR_MESSAGE);
									}
								});
							} catch (Exception e2) {
								e2.printStackTrace();
								//Shouldn't happen.
								throw new RuntimeException(e2);
							}
						}
					}
				}
				
				if (successes.get() > 0) {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override public void run() {
								appWindow.setContentPane(appWindowContent);
								appWindow.pack();
								StringBuilder installSpecific = new StringBuilder();
								for (String installSpecificMessage : installSpecificMessages) {
									installSpecific.append("<br>").append(installSpecificMessage);
								}
								showSuccess(installSpecific.toString());
							}
						});
					} catch (Exception e) {
						// Shouldn't happen.
						throw new RuntimeException(e);
					}
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						@Override public void run() {
							synchronized (exitMarker) {
								exitMarker.set(failure.get() ? 1 : 0);
								exitMarker.notifyAll();
							}
						}
					});
				}
			}
		}.start();
	}
	
	private void doUninstall() {
		JPanel spinner = new JPanel();
		spinner.setOpaque(true);
		spinner.setLayout(new FlowLayout());
		spinner.add(new JLabel(new ImageIcon(Installer.class.getResource("/lombok/installer/loading.gif"))));
		
		final Container originalContentPane = appWindow.getContentPane();
		appWindow.setContentPane(spinner);
		
		final AtomicInteger successes = new AtomicInteger();
		new Thread(new Runnable() {
			@Override public void run() {
				for (IdeLocation loc : toUninstall) {
					try {
						loc.uninstall();
						successes.incrementAndGet();
					} catch (final UninstallException e) {
						if (e.isWarning()) {
							try {
								SwingUtilities.invokeAndWait(new Runnable() {
									@Override public void run() {
										JOptionPane.showMessageDialog(appWindow,
												e.getMessage(), "Uninstall Problem", JOptionPane.WARNING_MESSAGE);
									}
								});
							} catch (Exception e2) {
								e2.printStackTrace();
								//Shouldn't happen.
								throw new RuntimeException(e2);
							}
						} else {
							try {
								SwingUtilities.invokeAndWait(new Runnable() {
									@Override public void run() {
										JOptionPane.showMessageDialog(appWindow,
												e.getMessage(), "Uninstall Problem", JOptionPane.ERROR_MESSAGE);
									}
								});
							} catch (Exception e2) {
								e2.printStackTrace();
								//Shouldn't happen.
								throw new RuntimeException(e2);
							}
						}
					}
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						if (successes.get() > 0) {
							JOptionPane.showMessageDialog(appWindow, "Lombok has been removed from the selected IDE installations.", "Uninstall successful", JOptionPane.INFORMATION_MESSAGE);
							appWindow.setVisible(false);
							System.exit(0);
							return;
						}
						
						appWindow.setContentPane(originalContentPane);
					}
				});
				
				
			}
		}).start();
	}
	
	private IdesList idesList = new IdesList();
	
	void selectedLomboksChanged(List<IdeLocation> selectedIdes) {
		boolean uninstallAvailable = false;
		boolean installAvailable = false;
		for (IdeLocation loc : selectedIdes) {
			if (loc.hasLombok()) uninstallAvailable = true;
			installAvailable = true;
		}
		
		uninstallButton.setVisible(uninstallAvailable);
		uninstallPlaceholder.setVisible(!uninstallAvailable);
		installButton.setEnabled(installAvailable);
	}
	
	private class IdesList extends JPanel implements Scrollable {
		private static final long serialVersionUID = 1L;
		
		List<IdeLocation> locations = new ArrayList<IdeLocation>();
		
		IdesList() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(Color.WHITE);
		}
		
		List<IdeLocation> getSelectedIdes() {
			List<IdeLocation> list = new ArrayList<IdeLocation>();
			for (IdeLocation loc : locations) if (loc.selected) list.add(loc);
			return list;
		}
		
		void fireSelectionChange() {
			selectedLomboksChanged(getSelectedIdes());
		}
		
		void addLocation(final IdeLocation location) {
			if (locations.contains(location)) return;
			Box box = Box.createHorizontalBox();
			box.setBackground(Color.WHITE);
			final JCheckBox checkbox = new JCheckBox(location.getName());
			checkbox.setBackground(Color.WHITE);
			box.add(new JLabel(new ImageIcon(location.getIdeIcon())));
			box.add(checkbox);
			checkbox.setSelected(true);
			checkbox.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					location.selected = checkbox.isSelected();
					fireSelectionChange();
				}
			});
			
			if (location.hasLombok()) {
				box.add(new JLabel(new ImageIcon(Installer.class.getResource("lombokIcon.png"))));
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
	}
	
	private void buildChrome(Container appWindowContainer) {
		JLabel leftGraphic = new JLabel(new ImageIcon(Installer.class.getResource("lombok.png")));
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		appWindowContainer.setLayout(new GridBagLayout());
		
		constraints.gridheight = 3;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(8, 8, 8, 8);
		appWindowContainer.add(leftGraphic, constraints);
		constraints.insets = new Insets(0, 0, 0, 0);
		
		constraints.gridx++;
		constraints.gridy++;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.ipadx = 16;
		constraints.ipady = 14;
		appWindowContainer.add(javacArea, constraints);
		
		constraints.gridy++;
		appWindowContainer.add(ideArea, constraints);
		
		appWindowContainer.add(uninstallArea, constraints);
		
		appWindowContainer.add(howIWorkArea, constraints);
		
		appWindowContainer.add(successArea, constraints);
		
		constraints.gridy++;
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.ipadx = 0;
		constraints.ipady = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.SOUTHWEST;
		constraints.insets = new Insets(0, 16, 8, 8);
		
		appWindow.add(buildButtonBar(), constraints);
	}

	private Box buildButtonBar() {
		Box buttonBar = Box.createHorizontalBox();

		JHyperLink aboutLink = new JHyperLink(Installer.ABOUT_LOMBOK_URL.toString());
		aboutLink.addActionListener(openBrowser(aboutLink, Installer.ABOUT_LOMBOK_URL));
		buttonBar.add(aboutLink);
		
		buttonBar.add(Box.createRigidArea(new Dimension(16, 1)));
		
		JLabel versionLabel = new JLabel();
		versionLabel.setText("v" + Version.getVersion());

		buttonBar.add(versionLabel);
		buttonBar.add(Box.createRigidArea(new Dimension(16, 1)));
		
		JHyperLink changelogLink = new JHyperLink("View full changelog");
		changelogLink.addActionListener(openBrowser(changelogLink, Installer.ABOUT_LOMBOK_URL.resolve("/changelog.html")));
		buttonBar.add(changelogLink);
		
		buttonBar.add(Box.createHorizontalGlue());
		
		JButton quitButton = new JButton("Quit Installer");
		quitButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				appWindow.setVisible(false);
				System.exit(0);
			}
		});
		buttonBar.add(quitButton);
		return buttonBar;
	}

	private ActionListener openBrowser(final JHyperLink hyperlink, final URI location) {
		return new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				hyperlink.setForeground(new Color(85, 145, 90));
				try {
					//java.awt.Desktop doesn't exist in 1.5.
					Object desktop = Class.forName("java.awt.Desktop").getMethod("getDesktop").invoke(null);
					Class.forName("java.awt.Desktop").getMethod("browse", URI.class).invoke(desktop, location);
				} catch (Exception e) {
					Runtime rt = Runtime.getRuntime();
					try {
						switch (OsUtils.getOS()) {
						case WINDOWS:
							String[] cmd = new String[4];
							cmd[0] = "cmd.exe";
							cmd[1] = "/C";
							cmd[2] = "start";
							cmd[3] = location.toString();
							rt.exec(cmd);
							break;
						case MAC_OS_X:
							rt.exec("open " + location.toString());
							break;
						default:
						case UNIX:
							rt.exec("firefox " + location.toString());
							break;
						}
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(appWindow,
								"Well, this is embarrassing. I don't know how to open a webbrowser.\n" +
								"I guess you'll have to open it. Browse to:\n" + location +
								" for more information about Lombok.",
								"I'm embarrassed", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		};
	}
	
	/**
	 * Makes the installer window visible.
	 */
	public void show() {
		appWindow.setVisible(true);
		if (OsUtils.getOS() == OS.MAC_OS_X) {
			try {
				AppleNativeLook.go();
			} catch (Throwable ignore) {
				//We're just prettying up the app. If it fails, meh.
			}
		}
	}
	
	private static final String IDE_TITLE =
		"<html><font size=\"+1\"><b><i>IDEs </i></b></font></html>";
	
	private static final String IDE_EXPLANATION =
		"<html>Lombok can update your Eclipse or eclipse-based IDE to fully support all Lombok features.<br>" +
		"Select IDE installations below and hit 'Install/Update'.</html>";
	
	private static final String IDE_LOADING_EXPLANATION =
		"Scanning your drives for IDE installations...";
	
	private static final String JAVAC_TITLE =
		"<html><font size=\"+1\"><b><i>Javac </i></b></font> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; (and tools that invoke javac such as <i>ant</i> and <i>maven</i>)</html>";
	
	private static final String JAVAC_EXPLANATION =
		"<html>Lombok works 'out of the box' with javac.<br>Just make sure the lombok.jar is in your classpath when you compile.";
	
	private static final String JAVAC_EXAMPLE =
		"<html>Example: <code>javac -cp lombok.jar MyCode.java</code></html>";
	
	private static final String UNINSTALL_TITLE =
		"<html><font size=\"+1\"><b><i>Uninstall </i></b></font></html>";
	
	private static final String UNINSTALL_EXPLANATION =
		"<html>Uninstall Lombok from the following IDE Installations?</html>";
	
	private static final String HOW_I_WORK_TITLE =
		"<html><font size=\"+1\"><b><i>What this installer does </i></b></font></html>";
	
	private static final String HOW_I_WORK_EXPLANATION =
		"<html><h2>Eclipse</h2><ol>" +
		"<li>First, I copy myself (lombok.jar) to your Eclipse install directory.</li>" +
		"<li>Then, I edit the <i>eclipse.ini</i> file to add the following entry:<br>" +
		"<pre>-javaagent:lombok.jar</pre></li></ol>" +
		"On Mac OS X, eclipse.ini is hidden in<br>" +
		"<code>Eclipse.app/Contents/MacOS</code> so that's where I place the jar files.</html>";
	
	private static final String SUCCESS_TITLE = "<html><font size=\"+1\"><b><i>Install successful </i></b></font></html>";
	private static final String SUCCESS_EXPLANATION = "<html>Lombok has been installed on the selected IDE installations.<br>" +
			"Don't forget to:<ul><li> add <code>lombok.jar</code> to your projects,<li><b>exit and start</b> your IDE,<li><b>rebuild</b> all projects!</ul>%%%</html>";
	
	
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
}
