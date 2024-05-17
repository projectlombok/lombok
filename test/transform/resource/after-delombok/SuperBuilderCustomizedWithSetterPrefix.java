import java.util.List;
public class SuperBuilderCustomizedWithSetterPrefix {
	public static class Parent {
		public static abstract class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private int field1;
			public B setField1(int field1) {
				this.field1 = field1 + 1;
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
				return "SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder(field1=" + this.field1 + ")";
			}
		}
		int field1;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl extends SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder<SuperBuilderCustomizedWithSetterPrefix.Parent, SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderCustomizedWithSetterPrefix.Parent build() {
				return new SuperBuilderCustomizedWithSetterPrefix.Parent(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder<?, ?> b) {
			this.field1 = b.field1;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder<?, ?> builder() {
			return new SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilderImpl();
		}
	}
	public static void test() {
		Parent x = Parent.builder().setField1(5).build();
	}
}
