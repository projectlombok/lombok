class WithOnClass1 {
	boolean isNone;
	boolean isPublic;
	WithOnClass1(boolean isNone, boolean isPublic) {
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public WithOnClass1 withPublic(final boolean isPublic) {
		return this.isPublic == isPublic ? this : new WithOnClass1(this.isNone, isPublic);
	}
}
class WithOnClass2 {
	boolean isNone;
	boolean isProtected;
	boolean isPackage;
	WithOnClass2(boolean isNone, boolean isProtected, boolean isPackage) {
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	protected WithOnClass2 withProtected(final boolean isProtected) {
		return this.isProtected == isProtected ? this : new WithOnClass2(this.isNone, isProtected, this.isPackage);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	WithOnClass2 withPackage(final boolean isPackage) {
		return this.isPackage == isPackage ? this : new WithOnClass2(this.isNone, this.isProtected, isPackage);
	}
}
class WithOnClass3 {
	String couldBeNull;
	@lombok.NonNull
	String nonNull;
	WithOnClass3(String couldBeNull, String nonNull) {
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public WithOnClass3 withCouldBeNull(final String couldBeNull) {
		return this.couldBeNull == couldBeNull ? this : new WithOnClass3(couldBeNull, this.nonNull);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public WithOnClass3 withNonNull(@lombok.NonNull final String nonNull) {
		if (nonNull == null) {
			throw new java.lang.NullPointerException("nonNull is marked non-null but is null");
		}
		return this.nonNull == nonNull ? this : new WithOnClass3(this.couldBeNull, nonNull);
	}
}
class WithOnClass4 {
	final int fX = 10;
	final int fY;
	WithOnClass4(int y) {
		this.fY = y;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public WithOnClass4 withY(final int fY) {
		return this.fY == fY ? this : new WithOnClass4(fY);
	}
}
