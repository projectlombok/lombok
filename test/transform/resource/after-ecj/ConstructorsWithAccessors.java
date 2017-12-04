@lombok.AllArgsConstructor @lombok.experimental.Accessors(prefix = {"p", "_"}) class ConstructorsWithAccessors {
  int plower;
  int pUpper;
  int _huh;
  int __huh2;
  public @java.lang.SuppressWarnings("all") ConstructorsWithAccessors(final int plower, final int upper, final int huh, final int _huh2) {
    super();
    this.plower = plower;
    this.pUpper = upper;
    this._huh = huh;
    this.__huh2 = _huh2;
  }
}
@lombok.AllArgsConstructor @lombok.experimental.Accessors(prefix = {"p", "_"}) class ConstructorsWithAccessorsNonNull {
  @lombok.NonNull Integer plower;
  @lombok.NonNull Integer pUpper;
  @lombok.NonNull Integer _huh;
  final @lombok.NonNull Integer __huh2;
  public @java.lang.SuppressWarnings("all") ConstructorsWithAccessorsNonNull(final @lombok.NonNull Integer plower, final @lombok.NonNull Integer upper, final @lombok.NonNull Integer huh, final @lombok.NonNull Integer _huh2) {
    super();
    if ((plower == null))
        {
          throw new java.lang.NullPointerException("plower");
        }
    if ((upper == null))
        {
          throw new java.lang.NullPointerException("upper");
        }
    if ((huh == null))
        {
          throw new java.lang.NullPointerException("huh");
        }
    if ((_huh2 == null))
        {
          throw new java.lang.NullPointerException("_huh2");
        }
    this.plower = plower;
    this.pUpper = upper;
    this._huh = huh;
    this.__huh2 = _huh2;
  }
}