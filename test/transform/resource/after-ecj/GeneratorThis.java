public class GeneratorThis {
  int variable = 2;
  public GeneratorThis() {
    super();
  }
  public @lombok.experimental.Generator Iterable<Integer> yieldThisField() {
    final class __Generator extends lombok.Lombok.Generator<Integer> {
      __Generator() {
      }
      protected void advance() {
        yieldThis(GeneratorThis.this.variable);
      }
    }
    return new __Generator();
  }
}
