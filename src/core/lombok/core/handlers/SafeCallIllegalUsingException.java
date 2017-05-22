package lombok.core.handlers;

import lombok.experimental.SafeCall;

/**
 * Created by Bulgakov Alexander on 22.04.17.
 */
public class SafeCallIllegalUsingException extends RuntimeException {
	public static final String PREFIX = "'" + SafeCall.class.getSimpleName() + "'";
	private final Object node;
	
	public SafeCallIllegalUsingException(String message, Object node) {
		super(message);
		this.node = node;
	}
	
	public SafeCallIllegalUsingException(MsgBuilder place, Object node) {
		this(place.message(node), node);
	}
	
	private static String getNodeType(Object node) {
		return node != null ? " " + node.getClass().getSimpleName() : "";
	}
	
	public static String unsupportedUnaryOperatorSymbol(Object node, Object operatorSymbol) {
		return PREFIX + " doesn't support operator " + operatorSymbol + " in " + getNodeType(node);
	}
	
	public static String unsupportedUnaryOperatorType(Object node, Object operatorType) {
		return PREFIX + " doesn't support operator type " + operatorType + " in " + getNodeType(node);
	}
	
	public static String incorrectTrueExprReference(Object invalid, Object valid) {
		return PREFIX + ". Invalid reference " + invalid + ". Must be " + valid;
	}
	
	public static String incorrectTrueExprType(Class type, Object truePart) {
		return PREFIX + ". Invalid reference type '" + type.getSimpleName() + "' of expression '" + truePart + "'";
	}
	
	public static String incorrectFalseExprType(Class type) {
		return PREFIX + ". Invalid default value type '" + type.getSimpleName() +
				"'. Must be a reference, literal constant or static field access";
	}
	
	public static String incorrectFalseNotPrimitive(Object type) {
		return PREFIX + ". The default value must be a primitive but has type '" + type + "'";
	}
	
	public Object getNode() {
		return node;
	}
	
	public enum MsgBuilder {
		forLoopInitializer,
		forLoopVariable,
		tryResource,
		
		unsupportedExpression {
			@Override
			public String message(Object node) {
				return PREFIX + " doesn't support expressions of type" + getNodeType(node);
			}
		},
		
		unsupportedConditionExpression {
			@Override
			public String message(Object node) {
				return PREFIX + " doesn't support condition expressions of type" + getNodeType(node);
			}
		};;
		
		public String message(Object node) {
			return PREFIX + " doesn't supported here. " + this;
		}
	}
}
