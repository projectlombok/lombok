import com.google.common.collect.ImmutableList;
public class BuilderWithDeprecatedAnnOnly {
	@Deprecated
	int dep1;
	@Deprecated
	java.util.List<String> strings;
	@Deprecated
	ImmutableList<Integer> numbers;
	@java.lang.SuppressWarnings("all")
	BuilderWithDeprecatedAnnOnly(final int dep1, final java.util.List<String> strings, final ImmutableList<Integer> numbers) {
		this.dep1 = dep1;
		this.strings = strings;
		this.numbers = numbers;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderWithDeprecatedAnnOnlyBuilder {
		@java.lang.SuppressWarnings("all")
		private int dep1;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> strings;
		@java.lang.SuppressWarnings("all")
		private com.google.common.collect.ImmutableList.Builder<Integer> numbers;
		@java.lang.SuppressWarnings("all")
		BuilderWithDeprecatedAnnOnlyBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.Deprecated
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder dep1(final int dep1) {
			this.dep1 = dep1;
			return this;
		}
		@java.lang.Deprecated
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder string(final String string) {
			if (this.strings == null) this.strings = new java.util.ArrayList<String>();
			this.strings.add(string);
			return this;
		}
		@java.lang.Deprecated
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder strings(final java.util.Collection<? extends String> strings) {
			if (strings == null) {
				throw new java.lang.NullPointerException("strings cannot be null");
			}
			if (this.strings == null) this.strings = new java.util.ArrayList<String>();
			this.strings.addAll(strings);
			return this;
		}
		@java.lang.Deprecated
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder clearStrings() {
			if (this.strings != null) this.strings.clear();
			return this;
		}
		@java.lang.Deprecated
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder number(final Integer number) {
			if (this.numbers == null) this.numbers = com.google.common.collect.ImmutableList.builder();
			this.numbers.add(number);
			return this;
		}
		@java.lang.Deprecated
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder numbers(final java.lang.Iterable<? extends Integer> numbers) {
			if (numbers == null) {
				throw new java.lang.NullPointerException("numbers cannot be null");
			}
			if (this.numbers == null) this.numbers = com.google.common.collect.ImmutableList.builder();
			this.numbers.addAll(numbers);
			return this;
		}
		@java.lang.Deprecated
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder clearNumbers() {
			this.numbers = null;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithDeprecatedAnnOnly build() {
			java.util.List<String> strings;
			switch (this.strings == null ? 0 : this.strings.size()) {
			case 0: 
				strings = java.util.Collections.emptyList();
				break;
			case 1: 
				strings = java.util.Collections.singletonList(this.strings.get(0));
				break;
			default: 
				strings = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.strings));
			}
			com.google.common.collect.ImmutableList<Integer> numbers = this.numbers == null ? com.google.common.collect.ImmutableList.<Integer>of() : this.numbers.build();
			return new BuilderWithDeprecatedAnnOnly(this.dep1, strings, numbers);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder(dep1=" + this.dep1 + ", strings=" + this.strings + ", numbers=" + this.numbers + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder builder() {
		return new BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder();
	}
}
