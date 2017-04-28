package first.second;

class SafeCallGettersFields {
	public SafeCallGettersFields nullSafeCall;
	public SafeCallGettersFields nullSafeCall2;
	public String nullString;
	public static String s;

	static {
		first.second.SafeCallGettersFields s3 = new SafeCallGettersFields();
		java.lang.String s2 = s3 != null ? s3.getNullString() : null;
		java.lang.String s1 = s2 != null ? s2.trim() : null;
		s = s1;
	}

	public String s2;

	{
		first.second.SafeCallGettersFields s24 = getNullSafeCall();
		first.second.SafeCallGettersFields s23 = s24 != null ? s24.nullSafeCall : null;
		java.lang.String s22 = s23 != null ? s23.getNullString() : null;
		java.lang.String s21 = s22 != null ? s22.trim() : null;
		s2 = s21;
	}

	public String s3;

	{
		first.second.SafeCallGettersFields s34 = nullSafeCall;
		first.second.SafeCallGettersFields s33 = s34 != null ? s34.getNullSafeCall() : null;
		java.lang.String s32 = s33 != null ? s33.getNullString() : null;
		java.lang.String s31 = s32 != null ? s32.trim() : null;
		s3 = s31;
	}

	public String s4;
	public String s5 = "";
	public SafeCallGettersFields s1 = nullSafeCall;

	public SafeCallGettersFields() {
		java.lang.String s;
		{
			first.second.SafeCallGettersFields s3 = new first.second.SafeCallGettersFields();
			java.lang.String s2 = s3 != null ? s3.getNullString() : null;
			java.lang.String s1 = s2 != null ? s2.trim() : null;
			s = s1;
		}
		int s11 = 0;
		{
			String s13 = "";
			{
				String s1;
				{
					first.second.SafeCallGettersFields s16 = getNullSafeCall();
					first.second.SafeCallGettersFields s15 = s16 != null ? s16.getNullSafeCall2() : null;
					java.lang.String s14 = s15 != null ? s15.getNullString() : null;
					java.lang.String s12 = s14 != null ? s14.trim() : null;
					s1 = s12;
				}
			}
		}
		SafeCallGettersFields nullSafeCall = getNullSafeCall();
		String s2;
		{
			first.second.SafeCallGettersFields s24 = nullSafeCall;
			first.second.SafeCallGettersFields s23 = s24 != null ? s24.nullSafeCall2 : null;
			java.lang.String s22 = s23 != null ? s23.getNullString() : null;
			java.lang.String s21 = s22 != null ? s22.trim() : null;
			s2 = s21;
		}
		String s3;
		{
			first.second.SafeCallGettersFields s34 = nullSafeCall;
			first.second.SafeCallGettersFields s33 = s34 != null ? s34.nullSafeCall : null;
			java.lang.String s32 = s33 != null ? s33.nullString : null;
			java.lang.String s31 = s32 != null ? s32.trim() : null;
			s3 = s31;
		}
		String s4;
		{
			first.second.SafeCallGettersFields s45 = nullSafeCall;
			first.second.SafeCallGettersFields s44 = s45 != null ? s45.getNullSafeCall() : null;
			first.second.SafeCallGettersFields s43 = s44 != null ? s44.nullSafeCall : null;
			java.lang.String s42 = s43 != null ? s43.nullString : null;
			java.lang.String s41 = s42 != null ? s42.trim() : null;
			s4 = s41;
		}
		String s5;
		{
			first.second.SafeCallGettersFields s53 = this.nullSafeCall;
			java.lang.String s52 = s53 != null ? s53.nullString : null;
			java.lang.String s51 = s52 != null ? s52.trim() : null;
			s5 = s51;
		}
	}

	public String getNullString() {
		return null;
	}

	public SafeCallGettersFields getNullSafeCall() {
		return null;
	}

	public SafeCallGettersFields getNullSafeCall2() {
		return null;
	}
}