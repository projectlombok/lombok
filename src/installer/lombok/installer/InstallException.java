package lombok.installer;

/**
 * Thrown when installation of lombok into an IDE fails.
 */
public class InstallException extends Exception {
	public InstallException(String message, Throwable cause) {
		super(message, cause);
	}
}
