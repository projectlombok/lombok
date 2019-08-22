// skip-idempotent
import java.util.List;
class CheckerFrameworkBuilder {
	int x;
	int y;
	int z;
	List<String> names;
	@java.lang.SuppressWarnings("all")
	private static int $default$x() {
		return 5;
	}
	@org.checkerframework.common.aliasing.qual.Unique
	@java.lang.SuppressWarnings("all")
	CheckerFrameworkBuilder(final int x, final int y, final int z, final List<String> names) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.names = names;
	}
	@java.lang.SuppressWarnings("all")
	public static class CheckerFrameworkBuilderBuilder {
		@java.lang.SuppressWarnings("all")
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		private int x$value;
		@java.lang.SuppressWarnings("all")
		private int y;
		@java.lang.SuppressWarnings("all")
		private int z;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> names;
		@org.checkerframework.common.aliasing.qual.Unique
		@java.lang.SuppressWarnings("all")
		CheckerFrameworkBuilderBuilder() {
		}
		@org.checkerframework.checker.builder.qual.ReturnsReceiver
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilderBuilder x(@org.checkerframework.checker.builder.qual.NotCalledMethods("x") CheckerFrameworkBuilderBuilder this, final int x) {
			this.x$value = x;
			x$set = true;
			return this;
		}
		@org.checkerframework.checker.builder.qual.ReturnsReceiver
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilderBuilder y(@org.checkerframework.checker.builder.qual.NotCalledMethods("y") CheckerFrameworkBuilderBuilder this, final int y) {
			this.y = y;
			return this;
		}
		@org.checkerframework.checker.builder.qual.ReturnsReceiver
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilderBuilder z(@org.checkerframework.checker.builder.qual.NotCalledMethods("z") CheckerFrameworkBuilderBuilder this, final int z) {
			this.z = z;
			return this;
		}
		@org.checkerframework.checker.builder.qual.ReturnsReceiver
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilderBuilder name(final String name) {
			if (this.names == null) this.names = new java.util.ArrayList<String>();
			this.names.add(name);
			return this;
		}
		@org.checkerframework.checker.builder.qual.ReturnsReceiver
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilderBuilder names(final java.util.Collection<? extends String> names) {
			if (this.names == null) this.names = new java.util.ArrayList<String>();
			this.names.addAll(names);
			return this;
		}
		@org.checkerframework.checker.builder.qual.ReturnsReceiver
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilderBuilder clearNames() {
			if (this.names != null) this.names.clear();
			return this;
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder build(@org.checkerframework.checker.builder.qual.CalledMethods({"y", "z"}) CheckerFrameworkBuilderBuilder this) {
			java.util.List<String> names;
			switch (this.names == null ? 0 : this.names.size()) {
			case 0: 
				names = java.util.Collections.emptyList();
				break;
			case 1: 
				names = java.util.Collections.singletonList(this.names.get(0));
				break;
			default: 
				names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.names));
			}
			int x$value = this.x$value;
			if (!x$set) x$value = CheckerFrameworkBuilder.$default$x();
			return new CheckerFrameworkBuilder(x$value, y, z, names);
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder(x$value=" + this.x$value + ", y=" + this.y + ", z=" + this.z + ", names=" + this.names + ")";
		}
	}
	@org.checkerframework.common.aliasing.qual.Unique
	@org.checkerframework.dataflow.qual.SideEffectFree
	@java.lang.SuppressWarnings("all")
	public static CheckerFrameworkBuilderBuilder builder() {
		return new CheckerFrameworkBuilderBuilder();
	}
}
