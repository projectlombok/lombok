class ValWithLocalClasses1 {
  {
    final @lombok.val ValWithLocalClasses2 f2 = new ValWithLocalClasses2() {
      x() {
        super();
      }
    };
  }
  ValWithLocalClasses1() {
    super();
  }
}
class ValWithLocalClasses2 {
  {
    final @lombok.val int f3 = 0;
  }
  ValWithLocalClasses2() {
    super();
  }
}
