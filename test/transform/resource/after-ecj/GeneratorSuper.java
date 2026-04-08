public class GeneratorSuper extends SuperClass {
  public GeneratorSuper() {
    super();
  }
  public @lombok.experimental.Generator Iterable<Integer> yieldSuperField() {
    final class __Generator extends lombok.Lombok.Generator<Integer> {
      __Generator() {
      }
      protected void advance() {
        yieldThis(GeneratorSuper.super.variable);
      }
    }
    return new __Generator();
  }
}

class SuperClass {
  int variable;
  SuperClass() {
    super();
  }
}
