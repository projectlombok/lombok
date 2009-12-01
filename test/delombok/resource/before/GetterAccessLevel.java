class Getter {
	@lombok.Getter(lombok.AccessLevel.NONE)
	boolean isNone;
	@lombok.Getter(lombok.AccessLevel.PRIVATE)
	boolean isPrivate;
	@lombok.Getter(lombok.AccessLevel.PACKAGE)
	boolean isPackage;
	@lombok.Getter(lombok.AccessLevel.PROTECTED)
	boolean isProtected;
	@lombok.Getter(lombok.AccessLevel.PUBLIC)
	boolean isPublic;
}
