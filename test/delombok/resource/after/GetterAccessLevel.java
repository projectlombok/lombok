class GetterAccessLevel {
	boolean isNone;
	boolean isPrivate;
	boolean isPackage;
	boolean isProtected;
	boolean isPublic;
	String noneString;
	String privateString;
	String packageString;
	String protectedString;
	String publicString;
	String value;
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
	private String getPrivateString() {
		return privateString;
	}
	String getPackageString() {
		return packageString;
	}
	protected String getProtectedString() {
		return protectedString;
	}
	public String getPublicString() {
		return publicString;
	}
	public String getValue() {
		return value;
	}
}