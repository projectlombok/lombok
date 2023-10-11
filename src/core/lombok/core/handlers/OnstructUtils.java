package lombok.core.handlers;

import lombok.Onstruct;

public class OnstructUtils {

	public static String varName(String requestedName, Onstruct instance) {
		String prefix = instance.pre();
		String suffix = instance.suf();
		boolean cml = instance.cml() && prefix != null && !prefix.isEmpty();
		return (prefix != null ? prefix : "") + (cml ? cml(requestedName) : requestedName) + (suffix != null ? suffix : "");
	}

	public static String methodName(String requestedName, Onstruct instance) {
		String methodPrefix = instance.methodPre();
		if (methodPrefix == null || methodPrefix.isEmpty()) return requestedName;
		return methodPrefix + (instance.methodCml() ? cml(requestedName) : requestedName);
	}

	public static String cml(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}
