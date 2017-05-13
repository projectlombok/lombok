package lombok.core.handlers;

import lombok.experimental.SafeCall;

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

	@Override
	public String getMessage() {
		return "'" + SafeCall.class.getSimpleName() + "' abort processing. " + place;
	}

	public Object getNode() {
		return node;
	}

	public enum Place {
		methodErrorType,
	}
}
