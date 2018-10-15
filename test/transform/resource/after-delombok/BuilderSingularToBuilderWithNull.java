class BuilderSingularToBuilderWithNull {
	private java.util.List<String> elems;
	public static void test() {
		new BuilderSingularToBuilderWithNull(null).toBuilder();
	}
	@java.lang.SuppressWarnings("all")
	BuilderSingularToBuilderWithNull(final java.util.List<String> elems) {
		this.elems = elems;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularToBuilderWithNullBuilder {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> elems;
		@java.lang.SuppressWarnings("all")
		BuilderSingularToBuilderWithNullBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularToBuilderWithNullBuilder elem(final String elem) {
			if (this.elems == null) this.elems = new java.util.ArrayList<String>();
			this.elems.add(elem);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularToBuilderWithNullBuilder elems(final java.util.Collection<? extends String> elems) {
			if (this.elems == null) this.elems = new java.util.ArrayList<String>();
			this.elems.addAll(elems);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularToBuilderWithNullBuilder clearElems() {
			if (this.elems != null) this.elems.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularToBuilderWithNull build() {
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
			return new BuilderSingularToBuilderWithNull(elems);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder(elems=" + this.elems + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderSingularToBuilderWithNullBuilder builder() {
		return new BuilderSingularToBuilderWithNullBuilder();
	}
	@java.lang.SuppressWarnings("all")
	public BuilderSingularToBuilderWithNullBuilder toBuilder() {
		final BuilderSingularToBuilderWithNullBuilder builder = new BuilderSingularToBuilderWithNullBuilder();
		if (this.elems != null) builder.elems(this.elems);
		return builder;
	}
}
