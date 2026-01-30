public class GeneratorThis {
    int variable = 2;

    @lombok.experimental.Generator
    public Iterable<Integer> yieldThisField() {
        yieldThis(this.variable);
    }
}
