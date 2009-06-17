package lombok.core;

public class TransformationsUtil {
	private TransformationsUtil() {}
	
	public static String toGetterName(CharSequence fieldName, boolean isBoolean) {
		final String prefix = isBoolean ? "is" : "get";
		
		if ( fieldName.length() == 0 ) return prefix;
		
		return buildName(prefix, fieldName.toString());
	}
	
	private static String buildName(String prefix, String suffix) {
		if ( suffix.length() == 0 ) return prefix;
		
		char first = suffix.charAt(0);
		if ( Character.isLowerCase(first) )
			suffix = String.format("%s%s", Character.toTitleCase(first), suffix.subSequence(1, suffix.length()));
		return String.format("%s%s", prefix, suffix);
	}
	
	public static String toSetterName(CharSequence fieldName) {
		return buildName("set", fieldName.toString());
	}
}
