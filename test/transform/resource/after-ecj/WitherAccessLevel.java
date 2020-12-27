import lombok.AccessLevel;
class WitherAccessLevel {
  @lombok.experimental.Wither(lombok.AccessLevel.NONE) boolean isNone;
  @lombok.experimental.Wither(AccessLevel.PRIVATE) boolean isPrivate;
  @lombok.experimental.Wither(lombok.AccessLevel.PACKAGE) boolean isPackage;
  @lombok.experimental.Wither(AccessLevel.PROTECTED) boolean isProtected;
  @lombok.experimental.Wither(lombok.AccessLevel.PUBLIC) boolean isPublic;
  @lombok.experimental.Wither(value = lombok.AccessLevel.PUBLIC) boolean value;
  WitherAccessLevel(boolean isNone, boolean isPrivate, boolean isPackage, boolean isProtected, boolean isPublic, boolean value) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  private @java.lang.SuppressWarnings("all") WitherAccessLevel withPrivate(final boolean isPrivate) {
    return ((this.isPrivate == isPrivate) ? this : new WitherAccessLevel(this.isNone, isPrivate, this.isPackage, this.isProtected, this.isPublic, this.value));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  @java.lang.SuppressWarnings("all") WitherAccessLevel withPackage(final boolean isPackage) {
    return ((this.isPackage == isPackage) ? this : new WitherAccessLevel(this.isNone, this.isPrivate, isPackage, this.isProtected, this.isPublic, this.value));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  protected @java.lang.SuppressWarnings("all") WitherAccessLevel withProtected(final boolean isProtected) {
    return ((this.isProtected == isProtected) ? this : new WitherAccessLevel(this.isNone, this.isPrivate, this.isPackage, isProtected, this.isPublic, this.value));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WitherAccessLevel withPublic(final boolean isPublic) {
    return ((this.isPublic == isPublic) ? this : new WitherAccessLevel(this.isNone, this.isPrivate, this.isPackage, this.isProtected, isPublic, this.value));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WitherAccessLevel withValue(final boolean value) {
    return ((this.value == value) ? this : new WitherAccessLevel(this.isNone, this.isPrivate, this.isPackage, this.isProtected, this.isPublic, value));
  }
}
