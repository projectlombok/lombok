public class SuperBuilderWithExcludes {
    @lombok.experimental.SuperBuilder
    public static class Parent<N extends Number> {
        @lombok.Builder.Exclude
        private long excludedParentField;
        private N numberField;
        private long randomFieldP;
    }

    @lombok.experimental.SuperBuilder
    public static class Child extends Parent<Integer> {
        private double doubleField;
        @lombok.Builder.Exclude
        private long excludedChildField;
        private long randomFieldC;
    }
}
