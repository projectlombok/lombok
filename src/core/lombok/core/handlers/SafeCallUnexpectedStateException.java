package lombok.core.handlers;

import lombok.experimental.SafeCall;

/**
 * Created by Bulgakov Alexander on 23.04.17.
 */
public class SafeCallUnexpectedStateException extends RuntimeException {
	private final Place place;
	private final Object var;
	private final Class type;
	public SafeCallUnexpectedStateException(Place place, Object var, Class type) {
		this.place = place;
		this.var = var;
		this.type = type;
	}

	@Override
	public String getMessage() {
		return getInternalMessage();
	}

	private String getInternalMessage() {
		return "Internal " + SafeCall.class.getSimpleName() + " error. " + place + " " + var + " " +
				(type != null ? type.getSimpleName() : "");
	}

	public enum Place {
		findDuplicateCandidates,
		elvisConditional,
		elvisConditionalMethodInvocation,
		newConditional,
		populateInitStatementsTokenAmount,
		populateInitStatementsMethodInvocation,
		populateInitStatements,
		cannotRecognizeType,
		copy

	}
}
