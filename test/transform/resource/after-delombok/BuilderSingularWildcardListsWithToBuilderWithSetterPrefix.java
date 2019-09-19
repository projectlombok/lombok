import java.util.List;
import java.util.Collection;
@Builder(toBuilder = true, setterPrefix = "with")
class BuilderSingularWildcardListsWithToBuilderWithSetterPrefix {
	private List<?> objects;
	private Collection<? extends Number> numbers;
	@java.lang.SuppressWarnings("all")
	BuilderSingularWildcardListsWithToBuilderWithSetterPrefix(final List<?> objects, final Collection<? extends Number> numbers) {
		this.objects = objects;
		this.numbers = numbers;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<java.lang.Object> objects;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<Number> numbers;
		@java.lang.SuppressWarnings("all")
		BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder withObject(final java.lang.Object object) {
			if (this.objects == null) this.objects = new java.util.ArrayList<java.lang.Object>();
			this.objects.add(object);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder withObjects(final java.util.Collection<?> objects) {
			if (this.objects == null) this.objects = new java.util.ArrayList<java.lang.Object>();
			this.objects.addAll(objects);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder clearObjects() {
			if (this.objects != null) this.objects.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder withNumber(final Number number) {
			if (this.numbers == null) this.numbers = new java.util.ArrayList<Number>();
			this.numbers.add(number);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder withNumbers(final java.util.Collection<? extends Number> numbers) {
			if (this.numbers == null) this.numbers = new java.util.ArrayList<Number>();
			this.numbers.addAll(numbers);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder clearNumbers() {
			if (this.numbers != null) this.numbers.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularWildcardListsWithToBuilderWithSetterPrefix build() {
			java.util.List<java.lang.Object> objects;
			switch (this.objects == null ? 0 : this.objects.size()) {
			case 0: 
				objects = java.util.Collections.emptyList();
				break;
			case 1: 
				objects = java.util.Collections.singletonList(this.objects.get(0));
				break;
			default: 
				objects = java.util.Collections.unmodifiableList(new java.util.ArrayList<java.lang.Object>(this.objects));
			}
			java.util.Collection<Number> numbers;
			switch (this.numbers == null ? 0 : this.numbers.size()) {
			case 0: 
				numbers = java.util.Collections.emptyList();
				break;
			case 1: 
				numbers = java.util.Collections.singletonList(this.numbers.get(0));
				break;
			default: 
				numbers = java.util.Collections.unmodifiableList(new java.util.ArrayList<Number>(this.numbers));
			}
			return new BuilderSingularWildcardListsWithToBuilderWithSetterPrefix(objects, numbers);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularWildcardListsWithToBuilderWithSetterPrefix.BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder(objects=" + this.objects + ", numbers=" + this.numbers + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder builder() {
		return new BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder();
	}
	@java.lang.SuppressWarnings("all")
	public BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder toBuilder() {
		final BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder builder = new BuilderSingularWildcardListsWithToBuilderWithSetterPrefixBuilder();
		if (this.objects != null) builder.withObjects(this.objects);
		if (this.numbers != null) builder.withNumbers(this.numbers);
		return builder;
	}
}
