class SetterAccessLevel {
	boolean isNone;
	boolean isPrivate;
	boolean isPackage;
	boolean isProtected;
	boolean isPublic;
	boolean value;
	private void setIsPrivate(final boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	void setIsPackage(final boolean isPackage) {
		this.isPackage = isPackage;
	}
	protected void setIsProtected(final boolean isProtected) {
		this.isProtected = isProtected;
	}
	public void setIsPublic(final boolean isPublic) {
		this.isPublic = isPublic;
	}
	public void setValue(final boolean value) {
		this.value = value;
	}
}
