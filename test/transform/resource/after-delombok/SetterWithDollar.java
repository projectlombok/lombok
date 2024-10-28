class SetterWithDollar1 {
	int $i;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void set$i(final int $i) {
		this.$i = $i;
	}
}
class SetterWithDollar2 {
	int $i;
	int i;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void set$i(final int $i) {
		this.$i = $i;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setI(final int i) {
		this.i = i;
	}
}
