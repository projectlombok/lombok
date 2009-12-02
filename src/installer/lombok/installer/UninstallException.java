package lombok.installer;

/**
 * Thrown when uninstallation of lombok into an IDE fails.
 */
public class UninstallException extends Exception {
	public UninstallException(String message, Throwable cause) {
		super(message, cause);
	}
}
