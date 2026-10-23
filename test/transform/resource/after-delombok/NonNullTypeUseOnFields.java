import lombok.NonNull;
class NonNullTypeUseOnFields {
	@NonNull
	String[] declaration;
	String @NonNull [] array;
	String @NonNull [][] outerArray;
	String[] @NonNull [] innerArray;
	java.lang.@NonNull String[] elements;
	java.util.List<@NonNull String> listElements;
	java.util.Map<@NonNull String, java.util.List<Object @NonNull []>> nested;
	@NonNull
	java.util.List<String> list;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields(@NonNull final String[] declaration, final String @NonNull [] array, final String @NonNull [][] outerArray, @NonNull final java.util.List<String> list) {
		if (declaration == null) {
			throw new java.lang.NullPointerException("declaration is marked non-null but is null");
		}
		if (array == null) {
			throw new java.lang.NullPointerException("array is marked non-null but is null");
		}
		if (outerArray == null) {
			throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
		}
		if (list == null) {
			throw new java.lang.NullPointerException("list is marked non-null but is null");
		}
		this.declaration = declaration;
		this.array = array;
		this.outerArray = outerArray;
		this.list = list;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields(@NonNull final String[] declaration, final String @NonNull [] array, final String @NonNull [][] outerArray, final String[] @NonNull [] innerArray, final java.lang.@NonNull String[] elements, final java.util.List<@NonNull String> listElements, final java.util.Map<@NonNull String, java.util.List<Object @NonNull []>> nested, @NonNull final java.util.List<String> list) {
		if (declaration == null) {
			throw new java.lang.NullPointerException("declaration is marked non-null but is null");
		}
		if (array == null) {
			throw new java.lang.NullPointerException("array is marked non-null but is null");
		}
		if (outerArray == null) {
			throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
		}
		if (list == null) {
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
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setDeclaration(@NonNull final String[] declaration) {
		if (declaration == null) {
			throw new java.lang.NullPointerException("declaration is marked non-null but is null");
		}
		this.declaration = declaration;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setArray(final String @NonNull [] array) {
		if (array == null) {
			throw new java.lang.NullPointerException("array is marked non-null but is null");
		}
		this.array = array;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setOuterArray(final String @NonNull [][] outerArray) {
		if (outerArray == null) {
			throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
		}
		this.outerArray = outerArray;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setInnerArray(final String[] @NonNull [] innerArray) {
		this.innerArray = innerArray;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setElements(final java.lang.@NonNull String[] elements) {
		this.elements = elements;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setListElements(final java.util.List<@NonNull String> listElements) {
		this.listElements = listElements;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setNested(final java.util.Map<@NonNull String, java.util.List<Object @NonNull []>> nested) {
		this.nested = nested;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setList(@NonNull final java.util.List<String> list) {
		if (list == null) {
			throw new java.lang.NullPointerException("list is marked non-null but is null");
		}
		this.list = list;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withDeclaration(@NonNull final String[] declaration) {
		if (declaration == null) {
			throw new java.lang.NullPointerException("declaration is marked non-null but is null");
		}
		return this.declaration == declaration ? this : new NonNullTypeUseOnFields(declaration, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withArray(final String @NonNull [] array) {
		if (array == null) {
			throw new java.lang.NullPointerException("array is marked non-null but is null");
		}
		return this.array == array ? this : new NonNullTypeUseOnFields(this.declaration, array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withOuterArray(final String @NonNull [][] outerArray) {
		if (outerArray == null) {
			throw new java.lang.NullPointerException("outerArray is marked non-null but is null");
		}
		return this.outerArray == outerArray ? this : new NonNullTypeUseOnFields(this.declaration, this.array, outerArray, this.innerArray, this.elements, this.listElements, this.nested, this.list);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withInnerArray(final String[] @NonNull [] innerArray) {
		return this.innerArray == innerArray ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, innerArray, this.elements, this.listElements, this.nested, this.list);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withElements(final java.lang.@NonNull String[] elements) {
		return this.elements == elements ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, elements, this.listElements, this.nested, this.list);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withListElements(final java.util.List<@NonNull String> listElements) {
		return this.listElements == listElements ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, this.elements, listElements, this.nested, this.list);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withNested(final java.util.Map<@NonNull String, java.util.List<Object @NonNull []>> nested) {
		return this.nested == nested ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, nested, this.list);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NonNullTypeUseOnFields withList(@NonNull final java.util.List<String> list) {
		if (list == null) {
			throw new java.lang.NullPointerException("list is marked non-null but is null");
		}
		return this.list == list ? this : new NonNullTypeUseOnFields(this.declaration, this.array, this.outerArray, this.innerArray, this.elements, this.listElements, this.nested, list);
	}
}
