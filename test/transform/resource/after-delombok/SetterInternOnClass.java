public class SetterInternOnClass {
	private String str1;
	private java.lang.String str2;
	private int int1;

	@java.lang.SuppressWarnings("all")
	public void setStr1(final String str1) {
		this.str1 = str1 == null ? null : str1.intern();
	}

	@java.lang.SuppressWarnings("all")
	public void setStr2(final java.lang.String str2) {
		this.str2 = str2 == null ? null : str2.intern();
	}

	@java.lang.SuppressWarnings("all")
	public void setInt1(final int int1) {
		this.int1 = int1;
	}
}