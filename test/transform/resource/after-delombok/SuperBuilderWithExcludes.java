public class SuperBuilderWithExcludes {

    public static class Parent<N extends Number> {
        private long excludedParentField;
        private N numberField;
        private long randomFieldP;


        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public static abstract class ParentBuilder<N extends Number, C extends SuperBuilderWithExcludes.Parent<N>, B extends SuperBuilderWithExcludes.Parent.ParentBuilder<N, C, B>> {
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private long excludedParentField;
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private N numberField;
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private long randomFieldP;

            /**
             * @return {@code this}.
             */
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public B numberField(final N numberField) {
                this.numberField = numberField;
                return self();
            }

            /**
             * @return {@code this}.
             */
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public B randomFieldP(final long randomFieldP) {
                this.randomFieldP = randomFieldP;
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
                return "SuperBuilderWithExcludes.Parent.ParentBuilder(excludedParentField=" + this.excludedParentField + ", numberField=" + this.numberField + ", randomFieldP=" + this.randomFieldP + ")";
            }
        }


        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private static final class ParentBuilderImpl<N extends Number> extends SuperBuilderWithExcludes.Parent.ParentBuilder<N, SuperBuilderWithExcludes.Parent<N>, SuperBuilderWithExcludes.Parent.ParentBuilderImpl<N>> {
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private ParentBuilderImpl() {
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            protected SuperBuilderWithExcludes.Parent.ParentBuilderImpl<N> self() {
                return this;
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public SuperBuilderWithExcludes.Parent<N> build() {
                return new SuperBuilderWithExcludes.Parent<N>(this);
            }
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        protected Parent(final SuperBuilderWithExcludes.Parent.ParentBuilder<N, ?, ?> b) {
            this.excludedParentField = b.excludedParentField;
            this.numberField = b.numberField;
            this.randomFieldP = b.randomFieldP;
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public static <N extends Number> SuperBuilderWithExcludes.Parent.ParentBuilder<N, ?, ?> builder() {
            return new SuperBuilderWithExcludes.Parent.ParentBuilderImpl<N>();
        }
    }


    public static class Child extends Parent<Integer> {
        private double doubleField;
        private long excludedChildField;
        private long randomFieldC;


        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public static abstract class ChildBuilder<C extends SuperBuilderWithExcludes.Child, B extends SuperBuilderWithExcludes.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<Integer, C, B> {
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private double doubleField;
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private long excludedChildField;
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private long randomFieldC;

            /**
             * @return {@code this}.
             */
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public B doubleField(final double doubleField) {
                this.doubleField = doubleField;
                return self();
            }

            /**
             * @return {@code this}.
             */
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public B randomFieldC(final long randomFieldC) {
                this.randomFieldC = randomFieldC;
                return self();
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            protected abstract B self();

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public abstract C build();

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public java.lang.String toString() {
                return "SuperBuilderWithExcludes.Child.ChildBuilder(super=" + super.toString() + ", doubleField=" + this.doubleField + ", excludedChildField=" + this.excludedChildField + ", randomFieldC=" + this.randomFieldC + ")";
            }
        }


        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private static final class ChildBuilderImpl extends SuperBuilderWithExcludes.Child.ChildBuilder<SuperBuilderWithExcludes.Child, SuperBuilderWithExcludes.Child.ChildBuilderImpl> {
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            private ChildBuilderImpl() {
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            protected SuperBuilderWithExcludes.Child.ChildBuilderImpl self() {
                return this;
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @lombok.Generated
            public SuperBuilderWithExcludes.Child build() {
                return new SuperBuilderWithExcludes.Child(this);
            }
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        protected Child(final SuperBuilderWithExcludes.Child.ChildBuilder<?, ?> b) {
            super(b);
            this.doubleField = b.doubleField;
            this.excludedChildField = b.excludedChildField;
            this.randomFieldC = b.randomFieldC;
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public static SuperBuilderWithExcludes.Child.ChildBuilder<?, ?> builder() {
            return new SuperBuilderWithExcludes.Child.ChildBuilderImpl();
        }
    }
}