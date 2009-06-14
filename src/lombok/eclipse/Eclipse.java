package lombok.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class Eclipse {
	private static final String DEFAULT_BUNDLE = "org.eclipse.jdt.core";
	public static void error(String message) {
		error(message, DEFAULT_BUNDLE, null);
	}
	
	public static void error(String message, Throwable error) {
		error(message, DEFAULT_BUNDLE, error);
	}
	
	public static void error(String message, String bundleName) {
		error(message, bundleName, null);
	}
	
	public static void error(String message, String bundleName, Throwable error) {
		Bundle bundle = Platform.getBundle(bundleName);
		if ( bundle == null ) {
			System.err.printf("Can't find bundle %s while trying to report error:\n%s\n", bundleName, message);
			return;
		}
		
		ILog log = Platform.getLog(bundle);
		
		log.log(new Status(IStatus.ERROR, bundleName, message, error));
	}
}
