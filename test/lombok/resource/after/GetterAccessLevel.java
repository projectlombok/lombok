class Getter {
	boolean isNone;
	boolean isPrivate;
	boolean isPackage;
	boolean isProtected;
	boolean isPublic;
	private boolean isPrivate() {
		return isPrivate;
	}
	boolean isPackage() {
		return isPackage;
	}
	protected boolean isProtected() {
		return isProtected;
	}
	public boolean isPublic() {
		return isPublic;
	}
}
