class SetterAccessLevel {
	boolean isNone;
	boolean isPrivate;
	boolean isPackage;
	boolean isProtected;
	boolean isPublic;
	boolean value;
	@java.lang.SuppressWarnings("all")
	private void setIsPrivate(final boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	@java.lang.SuppressWarnings("all")
	void setIsPackage(final boolean isPackage) {
		this.isPackage = isPackage;
	}
	@java.lang.SuppressWarnings("all")
	protected void setIsProtected(final boolean isProtected) {
		this.isProtected = isProtected;
	}
	@java.lang.SuppressWarnings("all")
	public void setIsPublic(final boolean isPublic) {
		this.isPublic = isPublic;
	}
	@java.lang.SuppressWarnings("all")
	public void setValue(final boolean value) {
		this.value = value;
	}
}