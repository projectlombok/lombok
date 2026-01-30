public class GeneratorSuper extends SuperClass {
    public Iterable<Integer> yieldSuperField() {

        final class __Generator extends lombok.Lombok.Generator<Integer> {
            protected void advance() {
                yieldThis(GeneratorSuper.super.variable);
            }
        }
        return new __Generator();
    }
}

class SuperClass {
    int variable;
}
