public class GeneratorSuper extends SuperClass {
    @lombok.experimental.Generator
    public Iterable<Integer> yieldSuperField() {
        yieldThis(super.variable);
    }
}

class SuperClass {
    int variable;
}
