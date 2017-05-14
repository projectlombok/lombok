package lombok.core.handlers;

import lombok.experimental.SafeCall;

/**
 * Created by Bulgakov Alexander on 22.04.17.
 */
public class SafeCallIllegalUsingException extends RuntimeException {
	public static final String PREFIX = "'" + SafeCall.class.getSimpleName() + "'";
	private final Place place;
	private final Object node;

	public SafeCallIllegalUsingException(Place place, Object node) {
		this.place = place;
		this.node = node;
	}

	public static String unsupportedPlaceMessage(Place place) {
		return PREFIX + " doesn't supported here. " + place;
	}

	@Override
	public String getMessage() {
		return place.getMessage(this);
	}

	public Place getPlace() {
		return place;
	}

	public Object getNode() {
		return node;
	}

	private String getNodeString() {
		return this.node != null ? " " + this.node.getClass().getSimpleName() : "";
	}

	public enum Place {
		forLoopInitializer,
		forLoopVariable,
		tryResource,
		unsupportedExpression {
			@Override
			public String getMessage(SafeCallIllegalUsingException e) {
				return PREFIX + " doesn't support expressions of type" + e.getNodeString();
			}
		};

		public String getMessage(SafeCallIllegalUsingException e) {
			return PREFIX + " doesn't support " +
					this + e.getNodeString();
		}
	}
}
