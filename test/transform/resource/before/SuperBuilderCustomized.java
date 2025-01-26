//version 8: Javac 6 will error out due to `ChildBuilder` not existing before properly running lombok. Giving j6 support status, not worth fixing.
import java.util.List;

public class SuperBuilderCustomized {
	@lombok.experimental.SuperBuilder
	public static class Parent<T> {
		public static abstract class ParentBuilder<T, C extends Parent<T>, B extends ParentBuilder<T, C, B>> {
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
		
		protected Parent(ParentBuilder<T, ?, ?> b) {
			if (b.field1 == 0)
				throw new IllegalArgumentException("field1 must be != 0");
			this.field1 = b.field1;
		}
		
		public static <T> SuperBuilderCustomized.Parent.ParentBuilder<T, ?, ?> builder(int field1) {
			return new SuperBuilderCustomized.Parent.ParentBuilderImpl<T>().field1(field1);
		}
	}
	
	@lombok.experimental.SuperBuilder
	public static class Child extends Parent<String> {
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
