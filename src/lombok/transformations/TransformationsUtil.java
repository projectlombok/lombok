package lombok.transformations;

public class TransformationsUtil {
	private TransformationsUtil() {}
	
	public static String toGetterName(CharSequence fieldName, boolean isBoolean) {
		final String prefix = isBoolean ? "is" : "get";
		final String suffix;
		
		if ( fieldName.length() == 0 ) return prefix;
		
		char first = fieldName.charAt(0);
		if ( Character.isLowerCase(first) )
			suffix = String.format("%s%s", Character.toTitleCase(first), fieldName.subSequence(1, fieldName.length()));
		else suffix = fieldName.toString();
		return String.format("%s%s", prefix, suffix);
	}
}
