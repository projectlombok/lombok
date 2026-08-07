import lombok.AccessLevel;

class WitherAccessLevel {
	@lombok.With(lombok.AccessLevel.NONE)
	boolean isNone;
	
	@lombok.With(AccessLevel.PRIVATE)
	boolean isPrivate;
	
	@lombok.With(lombok.AccessLevel.PACKAGE)
	boolean isPackage;
	
	@lombok.With(AccessLevel.PROTECTED)
	boolean isProtected;
	
	@lombok.With(lombok.AccessLevel.PUBLIC)
	boolean isPublic;
	
	@lombok.With(value=lombok.AccessLevel.PUBLIC)
	boolean value;
	
	WitherAccessLevel(boolean isNone, boolean isPrivate, boolean isPackage, boolean isProtected, boolean isPublic, boolean value) {
	}
}
