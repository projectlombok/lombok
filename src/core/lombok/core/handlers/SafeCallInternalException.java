package lombok.core.handlers;

/**
 * Created by Bulgakov Alexander on 05.05.17.
 */
public class SafeCallInternalException extends Exception {
	private final Object node;

	public SafeCallInternalException(Object node, String message) {
		super(message);
		this.node = node;
	}

	public Object getNode() {
		return node;
	}
}
