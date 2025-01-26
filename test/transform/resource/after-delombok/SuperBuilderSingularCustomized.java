import java.util.Set;
class SuperBuilderSingularCustomized {
	private Set<String> foos;
	public static abstract class SuperBuilderSingularCustomizedBuilder<C extends SuperBuilderSingularCustomized, B extends SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<C, B>> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<String> foos;
		public B custom(final String value) {
			return self();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B foo(final String foo) {
			if (this.foos == null) this.foos = new java.util.ArrayList<String>();
			this.foos.add(foo);
			return self();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B foos(final java.util.Collection<? extends String> foos) {
			if (foos == null) {
				throw new java.lang.NullPointerException("foos cannot be null");
			}
			if (this.foos == null) this.foos = new java.util.ArrayList<String>();
			this.foos.addAll(foos);
			return self();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B clearFoos() {
			if (this.foos != null) this.foos.clear();
			return self();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected abstract B self();
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public abstract C build();
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder(foos=" + this.foos + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final class SuperBuilderSingularCustomizedBuilderImpl extends SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<SuperBuilderSingularCustomized, SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilderImpl> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private SuperBuilderSingularCustomizedBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilderImpl self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderSingularCustomized build() {
			return new SuperBuilderSingularCustomized(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected SuperBuilderSingularCustomized(final SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<?, ?> b) {
		java.util.Set<String> foos;
		switch (b.foos == null ? 0 : b.foos.size()) {
		case 0: 
			foos = java.util.Collections.emptySet();
			break;
		case 1: 
			foos = java.util.Collections.singleton(b.foos.get(0));
			break;
		default: 
			foos = new java.util.LinkedHashSet<String>(b.foos.size() < 1073741824 ? 1 + b.foos.size() + (b.foos.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
			foos.addAll(b.foos);
			foos = java.util.Collections.unmodifiableSet(foos);
		}
		this.foos = foos;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<?, ?> builder() {
		return new SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilderImpl();
	}
}
