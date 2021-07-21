// skip-idempotent
//version 8:
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
			@org.checkerframework.dataflow.qual.Pure
			@java.lang.SuppressWarnings("all")
			protected abstract @org.checkerframework.common.returnsreceiver.qual.This B self();
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.SuppressWarnings("all")
			public abstract C build(CheckerFrameworkSuperBuilder.Parent.@org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) ParentBuilder<C, B> this);
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B x(final int x) {
				this.x$value = x;
				x$set = true;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B y(final int y) {
				this.y = y;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B z(final int z) {
				this.z = z;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B name(final String name) {
				if (this.names == null) this.names = new java.util.ArrayList<String>();
				this.names.add(name);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B names(final java.util.Collection<? extends String> names) {
				if (names == null) {
					throw new java.lang.NullPointerException("names cannot be null");
				}
				if (this.names == null) this.names = new java.util.ArrayList<String>();
				this.names.addAll(names);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B clearNames() {
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
			@java.lang.SuppressWarnings("all")
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@org.checkerframework.dataflow.qual.Pure
			@java.lang.SuppressWarnings("all")
			protected CheckerFrameworkSuperBuilder.Parent.@org.checkerframework.common.returnsreceiver.qual.This ParentBuilderImpl self() {
				return this;
			}
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public CheckerFrameworkSuperBuilder.Parent build(CheckerFrameworkSuperBuilder.Parent.@org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) ParentBuilderImpl this) {
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
		public static CheckerFrameworkSuperBuilder.Parent.@org.checkerframework.common.aliasing.qual.Unique ParentBuilder<?, ?> builder() {
			return new CheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl();
		}
	}
	public static class ZChild extends Parent {
		int a;
		int b;
		@java.lang.SuppressWarnings("all")
		private static int $default$a() {
			return 1;
		}
		@java.lang.SuppressWarnings("all")
		public static abstract class ZChildBuilder<C extends CheckerFrameworkSuperBuilder.ZChild, B extends CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
			@java.lang.SuppressWarnings("all")
			private boolean a$set;
			@java.lang.SuppressWarnings("all")
			private int a$value;
			@java.lang.SuppressWarnings("all")
			private int b;
			@java.lang.Override
			@org.checkerframework.dataflow.qual.Pure
			@java.lang.SuppressWarnings("all")
			protected abstract @org.checkerframework.common.returnsreceiver.qual.This B self();
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build(CheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.checker.calledmethods.qual.CalledMethods("b") ZChildBuilder<C, B> this);
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B a(final int a) {
				this.a$value = a;
				a$set = true;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public @org.checkerframework.common.returnsreceiver.qual.This B b(final int b) {
				this.b = b;
				return self();
			}
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder(super=" + super.toString() + ", a$value=" + this.a$value + ", b=" + this.b + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ZChildBuilderImpl extends CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<CheckerFrameworkSuperBuilder.ZChild, CheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private ZChildBuilderImpl() {
			}
			@java.lang.Override
			@org.checkerframework.dataflow.qual.Pure
			@java.lang.SuppressWarnings("all")
			protected CheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.common.returnsreceiver.qual.This ZChildBuilderImpl self() {
				return this;
			}
			@org.checkerframework.dataflow.qual.SideEffectFree
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public CheckerFrameworkSuperBuilder.ZChild build(CheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.checker.calledmethods.qual.CalledMethods("b") ZChildBuilderImpl this) {
				return new CheckerFrameworkSuperBuilder.ZChild(this);
			}
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		protected ZChild(final CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<?, ?> b) {
			super(b);
			if (b.a$set) this.a = b.a$value;
			 else this.a = CheckerFrameworkSuperBuilder.ZChild.$default$a();
			this.b = b.b;
		}
		@org.checkerframework.dataflow.qual.SideEffectFree
		@java.lang.SuppressWarnings("all")
		public static CheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.common.aliasing.qual.Unique ZChildBuilder<?, ?> builder() {
			return new CheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl();
		}
	}
}