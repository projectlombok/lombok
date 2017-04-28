class SafeCallDefaultPackage {
	public SafeCallDefaultPackage() {
		String s;
		{
			SafeCallDefaultPackage s3 = new SafeCallDefaultPackage();
			java.lang.String s2 = s3 != null ? s3.getNullString() : null;
			java.lang.String s1 = s2 != null ? s2.trim() : null;
			s = s1;
		}
	}

	public String getNullString() {
		return null;
	}
}
