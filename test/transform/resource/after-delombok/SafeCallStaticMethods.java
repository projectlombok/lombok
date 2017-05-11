package first.second;

class SafeCallStaticMethods {
	public static first.second.SafeCallStaticMethods nullSafeCall;
	public static SafeCallStaticMethods nullSafeCall2;
	public static String nullString;
	public String nullString2;

	static {
		String s7;
		{
			first.second.SafeCallStaticMethods s72 = first.second.SafeCallStaticMethods.nullSafeCall;
			java.lang.String s71 = s72 != null ? s72.nullString2 : null;
			s7 = s71;
		}
		String s22 = "";
		{
			String s2;
			{
				java.lang.String s23 = first.second.SafeCallStaticMethods.getNullString();
				java.lang.String s21 = s23 != null ? s23.trim() : null;
				s2 = s21;
			}
		}
		String s3;
		{
			java.lang.String s32 = first.second.SafeCallStaticMethods.nullString;
			java.lang.String s31 = s32 != null ? s32.trim() : null;
			s3 = s31;
		}
		String s4;
		{
			java.lang.String s42 = first.second.SafeCallStaticMethods.nullString;
			java.lang.String s41 = s42 != null ? s42.trim() : null;
			s4 = s41;
		}
		String s5;
		{
			java.lang.String s52 = first.second.SafeCallStaticMethods.nullString;
			java.lang.String s51 = s52 != null ? s52.trim() : null;
			s5 = s51;
		}
		String s6;
		{
			java.lang.String s62 = first.second.SafeCallStaticMethods.nullString;
			java.lang.String s61 = s62 != null ? s62.trim() : null;
			s6 = s61;
		}
	}

	{
		String s22 = "";
		{
			String s2;
			{
				java.lang.String s23 = first.second.SafeCallStaticMethods.getNullString();
				java.lang.String s21 = s23 != null ? s23.trim() : null;
				s2 = s21;
			}
		}
		String s3;
		{
			java.lang.String s32 = first.second.SafeCallStaticMethods.nullString;
			java.lang.String s31 = s32 != null ? s32.trim() : null;
			s3 = s31;
		}
		String s4;
		{
			java.lang.String s42 = first.second.SafeCallStaticMethods.nullString;
			java.lang.String s41 = s42 != null ? s42.trim() : null;
			s4 = s41;
		}
	}

	String s2;

	{
		java.lang.String s22 = first.second.SafeCallStaticMethods.getNullString();
		java.lang.String s21 = s22 != null ? s22.trim() : null;
		s2 = s21;
	}

	String s3;

	{
		java.lang.String s32 = first.second.SafeCallStaticMethods.nullString;
		java.lang.String s31 = s32 != null ? s32.trim() : null;
		s3 = s31;
	}

	String s4;

	{
		java.lang.String s42 = first.second.SafeCallStaticMethods.nullString;
		java.lang.String s41 = s42 != null ? s42.trim() : null;
		s4 = s41;
	}

	public SafeCallStaticMethods() {
		String s22 = "";
		{
			String s2;
			{
				java.lang.String s23 = first.second.SafeCallStaticMethods.getNullString();
				java.lang.String s21 = s23 != null ? s23.trim() : null;
				s2 = s21;
			}
		}
		String s3;
		{
			java.lang.String s32 = first.second.SafeCallStaticMethods.nullString;
			java.lang.String s31 = s32 != null ? s32.trim() : null;
			s3 = s31;
		}
		String s4;
		{
			java.lang.String s42 = first.second.SafeCallStaticMethods.nullString;
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

	public static first.second.SafeCallStaticMethods getNullSafeCall(Object arg) {
		return null;
	}

	public static Object getNullArg() {
		return null;
	}

	public static SafeCallStaticMethods getNullSafeCall2() {
		return null;
	}
}