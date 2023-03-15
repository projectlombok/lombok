public class GeneratorThis {
    int variable = 2;

    public Iterable<Integer> yieldThisField() {

        final class __Generator extends lombok.Lombok.Generator<Integer> {
            protected void advance() {
                yieldThis(GeneratorThis.this.variable);
            }
        }
        return new __Generator();
    }
}
