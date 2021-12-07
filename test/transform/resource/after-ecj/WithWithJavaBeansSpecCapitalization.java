@lombok.With class WithWithJavaBeansSpecCapitalization {
  int aField;
  WithWithJavaBeansSpecCapitalization(int aField) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithWithJavaBeansSpecCapitalization withaField(final int aField) {
    return ((this.aField == aField) ? this : new WithWithJavaBeansSpecCapitalization(aField));
  }
}
