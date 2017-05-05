package lombok.core.handlers;

/**
 * Created by Bulgakov Alexander on 05.05.17.
 */
public class SafeCallInternalException extends Exception {
	private final Object var;

	public SafeCallInternalException(Object var, String message) {
		super(message);
		this.var = var;
	}

	public Object getVar() {
		return var;
	}
}
