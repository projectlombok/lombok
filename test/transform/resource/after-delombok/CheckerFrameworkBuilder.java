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
		@java.lang.SuppressWarnings("all")
		CheckerFrameworkBuilderBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@org.checkerframework.common.returnsreceiver.qual.This
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder x(CheckerFrameworkBuilder.@org.checkerframework.checker.calledmethods.qual.NotCalledMethods("x") CheckerFrameworkBuilderBuilder this, final int x) {
			this.x$value = x;
			x$set = true;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@org.checkerframework.common.returnsreceiver.qual.This
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder y(CheckerFrameworkBuilder.@org.checkerframework.checker.calledmethods.qual.NotCalledMethods("y") CheckerFrameworkBuilderBuilder this, final int y) {
			this.y = y;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@org.checkerframework.common.returnsreceiver.qual.This
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder z(CheckerFrameworkBuilder.@org.checkerframework.checker.calledmethods.qual.NotCalledMethods("z") CheckerFrameworkBuilderBuilder this, final int z) {
			this.z = z;
			return this;
		}
		@org.checkerframework.common.returnsreceiver.qual.This
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder name(final String name) {
			if (this.names == null) this.names = new java.util.ArrayList<String>();
			this.names.add(name);
			return this;
		}
		@org.checkerframework.common.returnsreceiver.qual.This
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder names(final java.util.Collection<? extends String> names) {
			if (names == null) {
				throw new java.lang.NullPointerException("names cannot be null");
			}
			if (this.names == null) this.names = new java.util.ArrayList<String>();
			this.names.addAll(names);
			return this;
		}
		@org.checkerframework.common.returnsreceiver.qual.This
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder clearNames() {
			if (this.names != null) this.names.clear();
			return this;
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		public CheckerFrameworkBuilder build(CheckerFrameworkBuilder.@org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) CheckerFrameworkBuilderBuilder this) {
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
			if (!this.x$set) x$value = CheckerFrameworkBuilder.$default$x();
			return new CheckerFrameworkBuilder(x$value, this.y, this.z, names);
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder(x$value=" + this.x$value + ", y=" + this.y + ", z=" + this.z + ", names=" + this.names + ")";
		}
	}
	
	@org.checkerframework.dataflow.qual.SideEffectFree
	@java.lang.SuppressWarnings("all")
	public static CheckerFrameworkBuilder.@org.checkerframework.common.aliasing.qual.Unique CheckerFrameworkBuilderBuilder builder() {
		return new CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder();
	}
}
