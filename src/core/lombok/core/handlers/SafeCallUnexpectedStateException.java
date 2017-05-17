package lombok.core.handlers;

import lombok.experimental.SafeCall;

/**
 * Created by Bulgakov Alexander on 23.04.17.
 */
public class SafeCallUnexpectedStateException extends RuntimeException {
	private final Place place;
	private final Object node;
	private final Class type;
	public SafeCallUnexpectedStateException(Place place, Object node, Class type) {
		this.place = place;
		this.node = node;
		this.type = type;
	}

	@Override
	public String getMessage() {
		return getInternalMessage();
	}

	private String getInternalMessage() {
		return "Internal " + SafeCall.class.getSimpleName() + " error. " + place + " " + node + " " +
				(type != null ? type.getSimpleName() : "");
	}

	public Object getNode() {
		return node;
	}

	public enum Place {
		findDuplicateCandidates,
		elvisConditional,
		elvisConditionalMethodInvocation,
		newConditional,
		populateInitStatementsTokenAmount,
		populateInitStatementsMethodInvocation,
		unsupportedMethodType,
		populateInitStatements,
		cannotRecognizeType,
		copyExpr,
		addBlockAfterVarDec,
		getParent,
		unsupportedUnaryOperator,
		unsupportedUnaryOperatorType,
		getBindings,
		insertBlockAfterVariable
	}
}
