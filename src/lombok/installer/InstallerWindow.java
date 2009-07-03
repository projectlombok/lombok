package lombok.installer;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import lombok.core.Version;

public class InstallerWindow {
	private JFrame jFrame;
	private JLabel mainText;
	private JLabel leftGraphic, topGraphic;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new InstallerWindow().show();
			}
		});
	}
	
	public InstallerWindow() {
		jFrame = new JFrame(String.format("Project Lombok v%s - Installer", Version.getVersion()));
		
		//We want to offer an undo feature when the user cancels in the middle of an operation.
		jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		leftGraphic = new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/lombok.png")));
		topGraphic = new JLabel(new ImageIcon(InstallerWindow.class.getResource("/lombok/installer/lombokText.png")));
		mainText = new JLabel("Explanatory stuff goes here.");
		
		jFrame.setLayout(new BorderLayout());
		jFrame.add(leftGraphic, BorderLayout.WEST);
		jFrame.add(topGraphic, BorderLayout.NORTH);
		jFrame.add(mainText, BorderLayout.CENTER);
		
		
		jFrame.pack();
		System.out.println("WE SHOULD BE UP");
	}
	
	public void show() {
		jFrame.setVisible(true);
	}
}
