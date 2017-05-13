package lombok.core.handlers;

import lombok.experimental.SafeCall;

/**
 * Created by Bulgakov Alexander on 22.04.17.
 */
public class SafeCallIllegalUsingException extends Exception {
	private final Place place;
	private final Object node;

	public SafeCallIllegalUsingException(Place place, Object node) {
		this.place = place;
		this.node = node;
	}

	public static String unsupportedPlaceMessage(Place place) {
		return "'" + SafeCall.class.getSimpleName() + "' doesn't supported here. " + place;
	}

	@Override
	public String getMessage() {
		return "'" + SafeCall.class.getSimpleName() + "' doesn't support" +
				(place != null ? " " + place : "") +
				(node != null ? " " + node.getClass().getSimpleName() : "");
	}

	public Place getPlace() {
		return place;
	}

	public Object getNode() {
		return node;
	}

	public enum Place {
		forLoopInitializer,
		forLoopVariable,
		tryResource,
		unsupportedExpression
	}
}
