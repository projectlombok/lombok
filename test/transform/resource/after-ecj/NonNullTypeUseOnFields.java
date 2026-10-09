import lombok.NonNull;
@lombok.RequiredArgsConstructor @lombok.AllArgsConstructor @lombok.Setter @lombok.With class NonNullTypeUseOnFields {
  @NonNull String[] elementType;
  String @NonNull [] array;
  String @NonNull [][] outerArray;
  String[] @NonNull [] innerArray;
  java.lang.@NonNull String[] elements;
  java.util.List<@NonNull String> listElements;
  java.util.Map<@NonNull String, java.util.List<Object @NonNull []>> nested;
  java.util. @NonNull List<String> list;
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields(final String[] array, final String[][] outerArray, final java.util. @NonNull List<String> list) {
    super();
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
    this.array = array;
    this.outerArray = outerArray;
    this.list = list;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields(final @NonNull String[] elementType, final String[] array, final String[][] outerArray, final String[][] innerArray, final java.lang.@NonNull String[] elements, final java.util.List<@NonNull String> listElements, final java.util.Map<@NonNull String, java.util.List<Object[]>> nested, final java.util. @NonNull List<String> list) {
    super();
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
    this.elementType = elementType;
    this.array = array;
    this.outerArray = outerArray;
    this.innerArray = innerArray;
    this.elements = elements;
    this.listElements = listElements;
    this.nested = nested;
    this.list = list;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setElementType(final @NonNull String[] elementType) {
    this.elementType = elementType;
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
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setList(final java.util. @NonNull List<String> list) {
    if ((list == null))
        {
          throw new java.lang.NullPointerException("list is marked non-null but is null");
        }
    this.list = list;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withElementType(final @NonNull String[] elementType) {
    return ((this.elementType == elementType) ? this : new NonNullTypeUseOnFields(elementType, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withArray(final String[] array) {
    if ((array == null))
        {
          throw new java.lang.NullPointerException("array is marked non-null but is null");
        }
    return ((this.array == array) ? this : new NonNullTypeUseOnFields(this.elementType, array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withOuterArray(final String[][] outerArray) {
    if ((outerArray == null))
        {
          throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
        }
    return ((this.outerArray == outerArray) ? this : new NonNullTypeUseOnFields(this.elementType, this.array, outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withInnerArray(final String[][] innerArray) {
    return ((this.innerArray == innerArray) ? this : new NonNullTypeUseOnFields(this.elementType, this.array, this.outerArray, innerArray, this.elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withElements(final java.lang.@NonNull String[] elements) {
    return ((this.elements == elements) ? this : new NonNullTypeUseOnFields(this.elementType, this.array, this.outerArray, this.innerArray, elements, this.listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withListElements(final java.util.List<@NonNull String> listElements) {
    return ((this.listElements == listElements) ? this : new NonNullTypeUseOnFields(this.elementType, this.array, this.outerArray, this.innerArray, this.elements, listElements, this.nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withNested(final java.util.Map<@NonNull String, java.util.List<Object[]>> nested) {
    return ((this.nested == nested) ? this : new NonNullTypeUseOnFields(this.elementType, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, nested, this.list));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullTypeUseOnFields withList(final java.util. @NonNull List<String> list) {
    if ((list == null))
        {
          throw new java.lang.NullPointerException("list is marked non-null but is null");
        }
    return ((this.list == list) ? this : new NonNullTypeUseOnFields(this.elementType, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, list));
  }
}
