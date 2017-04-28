package first.second;

class SafeCallStaticMethods {
	public static first.second.SafeCallStaticMethods nullSafeCall;
	public static SafeCallStaticMethods nullSafeCall2;
	public static String nullString;

	static {
		String s22 = "";
		{
			String s2;
			{
				java.lang.String s23 = SafeCallStaticMethods.nullSafeCall2.getNullString();
				java.lang.String s21 = s23 != null ? s23.trim() : null;
				s2 = s21;
			}
		}
		String s3;
		{
			java.lang.String s32 = SafeCallStaticMethods.nullSafeCall.nullString;
			java.lang.String s31 = s32 != null ? s32.trim() : null;
			s3 = s31;
		}
		String s4;
		{
			java.lang.String s42 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString;
			java.lang.String s41 = s42 != null ? s42.trim() : null;
			s4 = s41;
		}
	}

	{
		String s22 = "";
		{
			String s2;
			{
				java.lang.String s23 = SafeCallStaticMethods.nullSafeCall2.getNullString();
				java.lang.String s21 = s23 != null ? s23.trim() : null;
				s2 = s21;
			}
		}
		String s3;
		{
			java.lang.String s32 = SafeCallStaticMethods.nullSafeCall.nullString;
			java.lang.String s31 = s32 != null ? s32.trim() : null;
			s3 = s31;
		}
		String s4;
		{
			java.lang.String s42 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString;
			java.lang.String s41 = s42 != null ? s42.trim() : null;
			s4 = s41;
		}
	}

	String s2;

	{
		java.lang.String s22 = SafeCallStaticMethods.nullSafeCall2.getNullString();
		java.lang.String s21 = s22 != null ? s22.trim() : null;
		s2 = s21;
	}

	String s3;

	{
		java.lang.String s32 = SafeCallStaticMethods.nullSafeCall.nullString;
		java.lang.String s31 = s32 != null ? s32.trim() : null;
		s3 = s31;
	}

	String s4;

	{
		java.lang.String s42 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString;
		java.lang.String s41 = s42 != null ? s42.trim() : null;
		s4 = s41;
	}

	public SafeCallStaticMethods() {
		String s22 = "";
		{
			String s2;
			{
				java.lang.String s23 = SafeCallStaticMethods.nullSafeCall2.getNullString();
				java.lang.String s21 = s23 != null ? s23.trim() : null;
				s2 = s21;
			}
		}
		String s3;
		{
			java.lang.String s32 = SafeCallStaticMethods.nullSafeCall.nullString;
			java.lang.String s31 = s32 != null ? s32.trim() : null;
			s3 = s31;
		}
		String s4;
		{
			java.lang.String s42 = SafeCallStaticMethods.getNullSafeCall().nullSafeCall.nullString;
			java.lang.String s41 = s42 != null ? s42.trim() : null;
			s4 = s41;
		}
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