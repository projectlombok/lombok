class NonNullOnSetterParameter {
  @lombok.Setter() String[] elements;
  @lombok.Setter() String value;
  NonNullOnSetterParameter() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setElements(final @lombok.NonNull String[] elements) {
    this.elements = elements;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setValue(final @lombok.NonNull String value) {
    if ((value == null))
        {
          throw new java.lang.NullPointerException("value is marked non-null but is null");
        }
    this.value = value;
  }
}
