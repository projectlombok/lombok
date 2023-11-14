public class SetterInternWithData {
	private String str1;

	@java.lang.SuppressWarnings("all")
	public SetterInternWithData() {
	}

	@java.lang.SuppressWarnings("all")
	public String getStr1() {
		return this.str1;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof SetterInternWithData)) return false;
		final SetterInternWithData other = (SetterInternWithData) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		final java.lang.Object this$str1 = this.getStr1();
		final java.lang.Object other$str1 = other.getStr1();
		if (this$str1 == null ? other$str1 != null : !this$str1.equals(other$str1)) return false;
		return true;
	}

	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof SetterInternWithData;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final java.lang.Object $str1 = this.getStr1();
		result = result * PRIME + ($str1 == null ? 43 : $str1.hashCode());
		return result;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "SetterInternWithData(str1=" + this.getStr1() + ")";
	}

	@java.lang.SuppressWarnings("all")
	public void setStr1(final String str1) {
		this.str1 = str1 == null ? null : str1.intern();
	}
}