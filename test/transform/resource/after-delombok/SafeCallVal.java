class SafeCallVal {
	public SafeCallVal() {
		final java.lang.String s;
		{
			SafeCallVal s3 = new SafeCallVal();
			java.lang.String s2 = s3 != null ? s3.getNullString() : null;
			java.lang.String s1 = s2 != null ? s2.trim() : null;
			s = s1;
		}
		final SafeCallVal nullSafeCall;
		{
			SafeCallVal nullSafeCall2 = new SafeCallVal();
			SafeCallVal nullSafeCall1 = nullSafeCall2 != null ? nullSafeCall2.getNullSafeCall() : null;
			nullSafeCall = nullSafeCall1;
		}
		final java.lang.String s1;
		{
			SafeCallVal s13 = nullSafeCall;
			java.lang.String s12 = s13 != null ? s13.getNullString() : null;
			java.lang.String s11 = s12 != null ? s12.trim() : null;
			s1 = s11;
		}
	}
	
	public SafeCallVal nullSafeCall;
	
	public String getNullString() {
		return null;
	}
	
	public SafeCallVal getNullSafeCall() {
		return null;
	}
}