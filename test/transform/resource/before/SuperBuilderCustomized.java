//version 8: Javac 6 will error out due to `ChildBuilder` not existing before properly running lombok. Giving j6 support status, not worth fixing.
import java.util.List;

public class SuperBuilderCustomized {
	@lombok.experimental.SuperBuilder
	public static class Parent {
		public static abstract class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
			public B resetToDefault() {
				field1 = 0;
				return self();
			}
			public B field1(int field1) {
				this.field1 = field1 + 1;
				return self();
			}
		}
		int field1;
		
		protected Parent(ParentBuilder<?, ?> b) {
			if (b.field1 == 0)
				throw new IllegalArgumentException("field1 must be != 0");
			this.field1 = b.field1;
		}
		
		public static SuperBuilderCustomized.Parent.ParentBuilder<?, ?> builder(int field1) {
			return new SuperBuilderCustomized.Parent.ParentBuilderImpl().field1(field1);
		}
	}
	
	@lombok.experimental.SuperBuilder
	public static class Child extends Parent {
		private static final class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
			@Override
			public Child build() {
				this.resetToDefault();
				return new Child(this);
			}
		}
		double field2;
		public static ChildBuilder<?, ?> builder() {
			return new ChildBuilderImpl().field2(10.0);
		}
	}
	
	public static void test() {
		Child x = Child.builder().field2(1.0).field1(5).resetToDefault().build();
	}
}
