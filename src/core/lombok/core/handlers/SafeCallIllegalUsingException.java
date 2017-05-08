package lombok.core.handlers;

import lombok.experimental.SafeCall;

/**
 * Created by Bulgakov Alexander on 22.04.17.
 */
public class SafeCallIllegalUsingException extends Exception {
	private final Place place;
	private final Object node;
	private final String operator;

	public SafeCallIllegalUsingException(Place place, Object node, Object parent) {
		this.place = place;
		this.node = node;
		operator = null;
	}

	public SafeCallIllegalUsingException(Place place, Object node, String operator) {
		this.place = place;
		this.node = node;
		this.operator = operator;
	}


	public SafeCallIllegalUsingException(Place place, Object node) {
		this.place = place;
		this.node = node;
		this.operator = null;
	}

	public static String unsupportedPlaceMessage(Place place) {
		return "'" + SafeCall.class.getSimpleName() + "' doesn't supported here. " + place;
	}

	public String illegalUsingMessage() {
		return "'" + SafeCall.class.getSimpleName() + "' doesn't support" +
				(place != null ? " " + place : "") +
				(node != null ? " " + node.getClass().getSimpleName() : "") +
				(operator != null ? " operator " + operator : "");
	}


	@Override
	public String getMessage() {
		return "place:" + getPlace() + "\nnode:" + node +
				(operator != null ? "\noperator:" + operator : "");
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
	}
}
