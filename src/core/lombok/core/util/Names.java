package lombok.core.util;

import static java.lang.Character.*;

public class Names {

	private Names() {};

	public static String camelCaseToConstant(final String fieldName) {
		if ( fieldName == null || fieldName.isEmpty() ) return "";
		char[] chars = fieldName.toCharArray();
		StringBuilder b = new StringBuilder();
		b.append(toUpperCase(chars[0]));
		for (int i = 1, iend = chars.length; i < iend; i++) {
			char c = chars[i];
			if (isUpperCase(c)) {
				b.append('_');
			} else {
				c = toUpperCase(c);
			}
			b.append(c);
		}
		return b.toString();
	}

}