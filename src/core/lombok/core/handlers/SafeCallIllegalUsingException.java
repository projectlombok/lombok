package lombok.core.handlers;

import lombok.experimental.SafeCall;

/**
 * Created by Bulgakov Alexander on 22.04.17.
 */
public class SafeCallIllegalUsingException extends Exception {
	private final Place place;
	private final Object variable;
	private final Object parent;

	public SafeCallIllegalUsingException(Place place, Object variable, Object parent) {
		this.place = place;
		this.variable = variable;
		this.parent = parent;
	}

	public static String unsupportedMessage(Place place) {
		return "'" + SafeCall.class.getSimpleName() + "' doesn't supported here. " + place;
	}

	@Override
	public String getMessage() {
		return "place:" + getPlace() + "\nvariable:" + variable + "\nparent:" + parent;
	}

	public Place getPlace() {
		return place;
	}

	public enum Place {
		forLoopInitializer,
		forLoopVariable,
		tryResource
	}
}
