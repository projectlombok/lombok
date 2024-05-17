class BuilderSingularToBuilderWithNullWithSetterPrefix {
	private java.util.List<String> elems;
	public static void test() {
		new BuilderSingularToBuilderWithNullWithSetterPrefix(null).toBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderSingularToBuilderWithNullWithSetterPrefix(final java.util.List<String> elems) {
		this.elems = elems;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderSingularToBuilderWithNullWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<String> elems;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderSingularToBuilderWithNullWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder withElem(final String elem) {
			if (this.elems == null) this.elems = new java.util.ArrayList<String>();
			this.elems.add(elem);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder withElems(final java.util.Collection<? extends String> elems) {
			if (elems == null) {
				throw new java.lang.NullPointerException("elems cannot be null");
			}
			if (this.elems == null) this.elems = new java.util.ArrayList<String>();
			this.elems.addAll(elems);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder clearElems() {
			if (this.elems != null) this.elems.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularToBuilderWithNullWithSetterPrefix build() {
			java.util.List<String> elems;
			switch (this.elems == null ? 0 : this.elems.size()) {
			case 0: 
				elems = java.util.Collections.emptyList();
				break;
			case 1: 
				elems = java.util.Collections.singletonList(this.elems.get(0));
				break;
			default: 
				elems = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.elems));
			}
			return new BuilderSingularToBuilderWithNullWithSetterPrefix(elems);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder(elems=" + this.elems + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder builder() {
		return new BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder toBuilder() {
		final BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder builder = new BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder();
		if (this.elems != null) builder.withElems(this.elems);
		return builder;
	}
}
