public class SetterInternOnField {
	private String str1;
	private java.lang.String str2;
	private String str3;

	@java.lang.SuppressWarnings("all")
	public void setStr3(final String str3) {
		this.str3 = str3;
	}

	@java.lang.SuppressWarnings("all")
	public void setStr1(final String str1) {
		this.str1 = str1 == null ? null : str1.intern();
	}
	
	@java.lang.SuppressWarnings("all")
	public void setStr2(final java.lang.String str2) {
		this.str2 = str2 == null ? null : str2.intern();
	}
}