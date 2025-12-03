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

	@lombok.experimental.SuperBuilder
	public static abstract class AbstractParent2<T extends Integer> {
	    @lombok.Getter
	    protected final T field1;
	
	    @lombok.Getter
	    @lombok.Builder.Default
	    protected final T field2 = null;
	
	    public static abstract class AbstractParent2Builder<
	            T extends Integer,
	            C extends AbstractParent2<T>,
	            B extends AbstractParent2Builder<T, C, B>> {
	        private B field2(final T ignored) {
	            return self();
	        }
	    }
	
	    public AbstractParent2(final AbstractParent2Builder<T, ?, ?> builder) {
	        this.field1 = builder.field1$value;
	        this.field2 = AbstractParent2.obtainField2();
	    }

		public static T obtainField2() {
			return (T) 123;
		}
	}
	
	@lombok.experimental.SuperBuilder
	public static class Child2<T extends Integer, TT extends Integer> extends AbstractParent2<T> {
	    @lombok.Getter
	    protected final T field3;
	
	    @lombok.Getter
	    @lombok.Builder.Default
	    protected final T field4 = null;
	
	    public static abstract class Child2Builder<
	            T extends Integer,
	            TT extends Integer,
	            C extends Child2<T, TT>,
	            B extends Child2Builder<T, TT, C, B>>
	            extends AbstractParent2Builder<T, C, B> {
	        private B field4(final TT ignored) {
	            return self();
	        }
	    }
	
	    public Child(final Child2Builder<T, TT, ?, ?> builder) {
	        super(builder);
	        this.field3 = builder.field3$value;
	        this.field4 = Child2.obtainField4();
	    }

		public static T obtainField4() {
			return (T) 456;
		}
	}
	
	public static void test() {
		Child x = Child.builder().field2(1.0).field1(5).resetToDefault().build();
		Child2 y = Child2.<Integer, Integer>builder().field1(234).field3(567).build();
	}
}
