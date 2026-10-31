//version 9:
public class BuilderWithDeprecatedMembers {
	@Deprecated(since = "1.2", forRemoval = true)
	int both;
	@Deprecated(forRemoval = true)
	java.util.List<String> strings;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithDeprecatedMembers(final int both, final java.util.List<String> strings) {
		this.both = both;
		this.strings = strings;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithDeprecatedMembersBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int both;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<String> strings;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithDeprecatedMembersBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.Deprecated(since = "1.2", forRemoval = true)
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder both(final int both) {
			this.both = both;
			return this;
		}
		@java.lang.Deprecated(forRemoval = true)
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder string(final String string) {
			if (this.strings == null) this.strings = new java.util.ArrayList<String>();
			this.strings.add(string);
			return this;
		}
		@java.lang.Deprecated(forRemoval = true)
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder strings(final java.util.Collection<? extends String> strings) {
			if (strings == null) {
				throw new java.lang.NullPointerException("strings cannot be null");
			}
			if (this.strings == null) this.strings = new java.util.ArrayList<String>();
			this.strings.addAll(strings);
			return this;
		}
		@java.lang.Deprecated(forRemoval = true)
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder clearStrings() {
			if (this.strings != null) this.strings.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithDeprecatedMembers build() {
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
			return new BuilderWithDeprecatedMembers(this.both, strings);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder(both=" + this.both + ", strings=" + this.strings + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder builder() {
		return new BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder();
	}
}
