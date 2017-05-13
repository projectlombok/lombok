package lombok.core.handlers;

/**
 * Created by Bulgakov Alexander on 13.05.17.
 */
public class SafeCallAbortProcessing extends Exception {
	private final Place place;
	private final Object node;

	public SafeCallAbortProcessing(Place place, Object node) {
		this.place = place;
		this.node = node;
	}

	public enum Place {
		methodErrorType,
	}
}
