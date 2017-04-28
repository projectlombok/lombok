package first.second;

import lombok.experimental.SafeCall;

class SafeCallStaticMethods {

	public static first.second.SafeCallStaticMethods nullSafeCall;
	public static SafeCallStaticMethods nullSafeCall2;
	public static String nullString;

	static {
		String s22 = "";
		{
			@SafeCall String s2 = SafeCallStaticMethods.nullSafeCall2.getNullString().trim();
		}
		@SafeCall String s3 = SafeCallStaticMethods.nullSafeCall.nullString.trim();
		@SafeCall String s4 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString.trim();
	}

	{
		String s22 = "";
		{
			@SafeCall String s2 = SafeCallStaticMethods.nullSafeCall2.getNullString().trim();
		}
		@SafeCall String s3 = SafeCallStaticMethods.nullSafeCall.nullString.trim();
		@SafeCall String s4 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString.trim();
	}

	@SafeCall String s2 = SafeCallStaticMethods.nullSafeCall2.getNullString().trim();
	@SafeCall String s3 = SafeCallStaticMethods.nullSafeCall.nullString.trim();
	@SafeCall String s4 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString.trim();

	public SafeCallStaticMethods() {
		String s22 = "";
		{
			@SafeCall String s2 = SafeCallStaticMethods.nullSafeCall2.getNullString().trim();
		}
		@SafeCall String s3 = SafeCallStaticMethods.nullSafeCall.nullString.trim();
		@SafeCall String s4 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString.trim();
	}

	public static String getNullString() {
		return null;
	}

	public static first.second.SafeCallStaticMethods getNullSafeCall() {
		return null;
	}
	public static SafeCallStaticMethods getNullSafeCall2() {
		return null;
	}
}