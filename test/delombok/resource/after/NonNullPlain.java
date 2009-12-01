class NonNullPlain {
	@lombok.NonNull
	int i;
	@lombok.NonNull
	String s;
	
	@lombok.NonNull
	public int getI() {
		return i;
	}
	
	public void setI(@lombok.NonNull final int i) {
		this.i = i;
	}
	
	@lombok.NonNull
	public String getS() {
		return s;
	}
	
	public void setS(@lombok.NonNull final String s) {
		if (s == null) throw new java.lang.NullPointerException("s");
		this.s = s;
	}
}
