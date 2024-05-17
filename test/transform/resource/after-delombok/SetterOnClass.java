//version 8:
class SetterOnClass1 {
	boolean isNone;
	boolean isPublic;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setPublic(final boolean isPublic) {
		this.isPublic = isPublic;
	}
}
class SetterOnClass2 {
	boolean isNone;
	boolean isProtected;
	boolean isPackage;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected void setProtected(final boolean isProtected) {
		this.isProtected = isProtected;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	void setPackage(final boolean isPackage) {
		this.isPackage = isPackage;
	}
}
class SetterOnClass3 {
	boolean isNone;
	boolean isPackage;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	void setPackage(final boolean isPackage) {
		this.isPackage = isPackage;
	}
}
class SetterOnClass4 {
	boolean isNone;
	boolean isPrivate;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private void setPrivate(final boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
}
class SetterOnClass5 {
	boolean isNone;
	boolean isPublic;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setPublic(final boolean isPublic) {
		this.isPublic = isPublic;
	}
}
class SetterOnClass6 {
	String couldBeNull;
	@lombok.NonNull
	String nonNull;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setCouldBeNull(final String couldBeNull) {
		this.couldBeNull = couldBeNull;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setNonNull(@lombok.NonNull final String nonNull) {
		if (nonNull == null) {
			throw new java.lang.NullPointerException("nonNull is marked non-null but is null");
		}
		this.nonNull = nonNull;
	}
}
