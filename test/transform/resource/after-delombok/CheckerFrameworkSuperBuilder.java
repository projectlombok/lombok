// skip-idempotent
import java.util.List;
class CheckerFrameworkSuperBuilder {
	public static class Parent {
		int x;
		int y;
		int z;
		List<String> names;
		@java.lang.SuppressWarnings("all")
		private static int $default$x() {
			return 5;
		}
		@java.lang.SuppressWarnings("all")
		public static abstract class ParentBuilder<C extends CheckerFrameworkSuperBuilder.Parent, B extends CheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B>> {
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
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.SuppressWarnings("all")
			public abstract C build(@org.checkerframework.checker.builder.qual.CalledMethods({"y", "z"}) CheckerFrameworkSuperBuilder.Parent this);
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B x(@org.checkerframework.checker.builder.qual.NotCalledMethods("x") CheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B> this, final int x) {
				this.x$value = x;
				x$set = true;
				return self();
			}
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B y(@org.checkerframework.checker.builder.qual.NotCalledMethods("y") CheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B> this, final int y) {
				this.y = y;
				return self();
			}
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B z(@org.checkerframework.checker.builder.qual.NotCalledMethods("z") CheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B> this, final int z) {
				this.z = z;
				return self();
			}
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B name(final String name) {
				if (this.names == null) this.names = new java.util.ArrayList<String>();
				this.names.add(name);
				return self();
			}
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B names(final java.util.Collection<? extends String> names) {
				if (names == null) {
					throw new java.lang.NullPointerException("names cannot be null");
				}
				if (this.names == null) this.names = new java.util.ArrayList<String>();
				this.names.addAll(names);
				return self();
			}
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B clearNames() {
				if (this.names != null) this.names.clear();
				return self();
			}
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "CheckerFrameworkSuperBuilder.Parent.ParentBuilder(x$value=" + this.x$value + ", y=" + this.y + ", z=" + this.z + ", names=" + this.names + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ParentBuilderImpl extends CheckerFrameworkSuperBuilder.Parent.ParentBuilder<CheckerFrameworkSuperBuilder.Parent, CheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl> {
			@org.checkerframework.common.aliasing.qual.Unique
			@java.lang.SuppressWarnings("all")
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.SuppressWarnings("all")
			protected CheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl self() {
				return this;
			}
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public CheckerFrameworkSuperBuilder.Parent build(@org.checkerframework.checker.builder.qual.CalledMethods({"y", "z"}) CheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl this) {
				return new CheckerFrameworkSuperBuilder.Parent(this);
			}
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		protected Parent(final CheckerFrameworkSuperBuilder.Parent.ParentBuilder<?, ?> b) {
			if (b.x$set) this.x = b.x$value;
			 else this.x = CheckerFrameworkSuperBuilder.Parent.$default$x();
			this.y = b.y;
			this.z = b.z;
			java.util.List<String> names;
			switch (b.names == null ? 0 : b.names.size()) {
			case 0: 
				names = java.util.Collections.emptyList();
				break;
			case 1: 
				names = java.util.Collections.singletonList(b.names.get(0));
				break;
			default: 
				names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.names));
			}
			this.names = names;
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		public static CheckerFrameworkSuperBuilder.Parent.ParentBuilder<?, ?> builder() {
			return new CheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl();
		}
	}
	public static class Child extends Parent {
		int a;
		int b;
		@java.lang.SuppressWarnings("all")
		private static int $default$a() {
			return 1;
		}
		@java.lang.SuppressWarnings("all")
		public static abstract class ChildBuilder<C extends CheckerFrameworkSuperBuilder.Child, B extends CheckerFrameworkSuperBuilder.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
			@java.lang.SuppressWarnings("all")
			private boolean a$set;
			@java.lang.SuppressWarnings("all")
			private int a$value;
			@java.lang.SuppressWarnings("all")
			private int b;
			@java.lang.Override
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build(@org.checkerframework.checker.builder.qual.CalledMethods("b") CheckerFrameworkSuperBuilder.Child this);
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B a(@org.checkerframework.checker.builder.qual.NotCalledMethods("a") CheckerFrameworkSuperBuilder.Child.ChildBuilder<C, B> this, final int a) {
				this.a$value = a;
				a$set = true;
				return self();
			}
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@java.lang.SuppressWarnings("all")
			public B b(@org.checkerframework.checker.builder.qual.NotCalledMethods("b") CheckerFrameworkSuperBuilder.Child.ChildBuilder<C, B> this, final int b) {
				this.b = b;
				return self();
			}
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "CheckerFrameworkSuperBuilder.Child.ChildBuilder(super=" + super.toString() + ", a$value=" + this.a$value + ", b=" + this.b + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ChildBuilderImpl extends CheckerFrameworkSuperBuilder.Child.ChildBuilder<CheckerFrameworkSuperBuilder.Child, CheckerFrameworkSuperBuilder.Child.ChildBuilderImpl> {
			@org.checkerframework.common.aliasing.qual.Unique
			@java.lang.SuppressWarnings("all")
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@org.checkerframework.checker.builder.qual.ReturnsReceiver
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.SuppressWarnings("all")
			protected CheckerFrameworkSuperBuilder.Child.ChildBuilderImpl self() {
				return this;
			}
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public CheckerFrameworkSuperBuilder.Child build(@org.checkerframework.checker.builder.qual.CalledMethods("b") CheckerFrameworkSuperBuilder.Child.ChildBuilderImpl this) {
				return new CheckerFrameworkSuperBuilder.Child(this);
			}
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		protected Child(final CheckerFrameworkSuperBuilder.Child.ChildBuilder<?, ?> b) {
			super(b);
			if (b.a$set) this.a = b.a$value;
			 else this.a = CheckerFrameworkSuperBuilder.Child.$default$a();
			this.b = b.b;
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		public static CheckerFrameworkSuperBuilder.Child.ChildBuilder<?, ?> builder() {
			return new CheckerFrameworkSuperBuilder.Child.ChildBuilderImpl();
		}
	}
}