package lombok.installer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Represents an installation location problem.
 * You should throw it upon creation of a {@code IdeLocation} class
 * if the provided location looks like your kind of IDE but there's something wrong with it.
 */
public class CorruptedIdeLocationException extends Exception {
	private final String ideType;
	
	public CorruptedIdeLocationException(String message, String ideType, Throwable cause) {
		super(message, cause);
		this.ideType = ideType;
	}
	
	public String getIdeType() {
		return ideType;
	}
	
	void showDialog(JFrame appWindow) {
		JOptionPane.showMessageDialog(appWindow, getMessage(), "Cannot configure " + ideType + " installation", JOptionPane.WARNING_MESSAGE);
	}
}
