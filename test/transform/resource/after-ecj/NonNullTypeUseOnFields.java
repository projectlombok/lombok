import lombok.NonNull;
@lombok.RequiredArgsConstructor @lombok.AllArgsConstructor @lombok.Setter @lombok.With class NonNullTypeUseOnFields {
  @NonNull String[] declaration;
  String @NonNull [] array;
  String @NonNull [][] outerArray;
  String[] @NonNull [] innerArray;
  java.lang.@NonNull String[] elements;
  java.util.List<@NonNull String> listElements;
  java.util.Map<@NonNull String, java.util.List<Object @NonNull []>> nested;
  @NonNull java.util.List<String> list;
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields(final @NonNull String[] declaration, final String[] array, final String[][] outerArray, final @NonNull java.util.List<String> list) {
    super();
    if ((declaration == null))
        {
          throw new java.lang.NullPointerException("declaration is marked non-null but is null");
        }
    if ((array == null))
        {
          throw new java.lang.NullPointerException("array is marked non-null but is null");
        }
    if ((outerArray == null))
        {
          throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
        }
    if ((list == null))
        {
          throw new java.lang.NullPointerException("list is marked non-null but is null");
        }
    this.declaration = declaration;
    this.array = array;
    this.outerArray = outerArray;
    this.list = list;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields(final @NonNull String[] declaration, final String[] array, final String[][] outerArray, final String[][] innerArray, final java.lang.@NonNull String[] elements, final java.util.List<@NonNull String> listElements, final java.util.Map<@NonNull String, java.util.List<Object[]>> nested, final @NonNull java.util.List<String> list) {
    super();
    if ((declaration == null))
        {
          throw new java.lang.NullPointerException("declaration is marked non-null but is null");
        }
    if ((array == null))
        {
          throw new java.lang.NullPointerException("array is marked non-null but is null");
        }
    if ((outerArray == null))
        {
          throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
        }
    if ((list == null))
        {
          throw new java.lang.NullPointerException("list is marked non-null but is null");
        }
    this.declaration = declaration;
    this.array = array;
    this.outerArray = outerArray;
    this.innerArray = innerArray;
    this.elements = elements;
    this.listElements = listElements;
    this.nested = nested;
    this.list = list;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setDeclaration(final @NonNull String[] declaration) {
    if ((declaration == null))
        {
          throw new java.lang.NullPointerException("declaration is marked non-null but is null");
        }
    this.declaration = declaration;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setArray(final String[] array) {
    if ((array == null))
        {
          throw new java.lang.NullPointerException("array is marked non-null but is null");
        }
    this.array = array;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setOuterArray(final String[][] outerArray) {
    if ((outerArray == null))
        {
          throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
        }
    this.outerArray = outerArray;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setInnerArray(final String[][] innerArray) {
    this.innerArray = innerArray;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setElements(final java.lang.@NonNull String[] elements) {
    this.elements = elements;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setListElements(final java.util.List<@NonNull String> listElements) {
    this.listElements = listElements;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setNested(final java.util.Map<@NonNull String, java.util.List<Object[]>> nested) {
    this.nested = nested;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setList(final @NonNull java.util.List<String> list) {
    if ((list == null))
        {
          throw new java.lang.NullPointerException("list is marked non-null but is null");
        }
    this.list = list;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withDeclaration(final @NonNull String[] declaration) {
    if ((declaration == null))
        {
          throw new java.lang.NullPointerException("declaration is marked non-null but is null");
        }
    return ((this.declaration == declaration) ? this : new NonNullTypeUseOnFields(declaration, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withArray(final String[] array) {
    if ((array == null))
        {
          throw new java.lang.NullPointerException("array is marked non-null but is null");
        }
    return ((this.array == array) ? this : new NonNullTypeUseOnFields(this.declaration, array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withOuterArray(final String[][] outerArray) {
    if ((outerArray == null))
        {
          throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
        }
    return ((this.outerArray == outerArray) ? this : new NonNullTypeUseOnFields(this.declaration, this.array, outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withInnerArray(final String[][] innerArray) {
    return ((this.innerArray == innerArray) ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withElements(final java.lang.@NonNull String[] elements) {
    return ((this.elements == elements) ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withListElements(final java.util.List<@NonNull String> listElements) {
    return ((this.listElements == listElements) ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, this.elements, listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withNested(final java.util.Map<@NonNull String, java.util.List<Object[]>> nested) {
    return ((this.nested == nested) ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withList(final @NonNull java.util.List<String> list) {
    if ((list == null))
        {
          throw new java.lang.NullPointerException("list is marked non-null but is null");
        }
    return ((this.list == list) ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, list));
  }
}
