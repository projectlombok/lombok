package lombok.core.handlers;

import lombok.Onstruct;
import lombok.Onstruct.Cml;

public class OnstructUtils {

	public static String varName(String requestedName, Onstruct instance) {
		String prefix = instance.pre();
		String suffix = instance.suf();
		boolean cml = instance.cml() && prefix != null && !prefix.isEmpty();
		return (prefix != null ? prefix : "") + (cml ? cml(requestedName) : requestedName) + (suffix != null ? suffix : "");
	}

	public static String methodName(String requestedName, Onstruct instance) {
		String methodPrefix = instance.source().pre;
		if (!instance.methodPre().equals(Onstruct.NULLSTRING)) methodPrefix = instance.methodPre();
		boolean cml = instance.source().cml;
		if (instance.methodCml() != Cml.SOURCE) cml = instance.methodCml().cml;
		if (methodPrefix == null || methodPrefix.isEmpty()) methodPrefix = "";
		return methodPrefix + (cml ? cml(requestedName) : requestedName);
	}

	public static String cml(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}
