@lombok.With @lombok.experimental.Accessors(javaBeansSpecCapitalization = true) class WithOnJavaBeansSpecCapitalization {
  int aField;
  WithOnJavaBeansSpecCapitalization(int aField) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithOnJavaBeansSpecCapitalization withaField(final int aField) {
    return ((this.aField == aField) ? this : new WithOnJavaBeansSpecCapitalization(aField));
  }
}

@lombok.With class WithOffJavaBeansSpecCapitalization {
  int aField;
  WithOffJavaBeansSpecCapitalization(int aField) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithOffJavaBeansSpecCapitalization withAField(final int aField) {
    return ((this.aField == aField) ? this : new WithOffJavaBeansSpecCapitalization(aField));
  }
}
