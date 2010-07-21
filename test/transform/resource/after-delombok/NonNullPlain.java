class NonNullPlain {
	@lombok.NonNull
	int i;
	@lombok.NonNull
	String s;
	@lombok.NonNull
	@java.lang.SuppressWarnings("all")
	public int getI() {
		return this.i;
	}
	@java.lang.SuppressWarnings("all")
	public void setI(@lombok.NonNull final int i) {
		this.i = i;
	}
	@lombok.NonNull
	@java.lang.SuppressWarnings("all")
	public String getS() {
		return this.s;
	}
	@java.lang.SuppressWarnings("all")
	public void setS(@lombok.NonNull final String s) {
		if (s == null) throw new java.lang.NullPointerException("s");
		this.s = s;
	}
}
