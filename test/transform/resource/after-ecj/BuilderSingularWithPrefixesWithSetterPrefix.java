import lombok.Singular;
@lombok.Builder(setterPrefix = "with") @lombok.experimental.Accessors(prefix = "_") class BuilderSingularWithPrefixesWithSetterPrefixes {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularWithPrefixesWithSetterPrefixesBuilder {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> elems;
    @java.lang.SuppressWarnings("all") BuilderSingularWithPrefixesWithSetterPrefixesBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularWithPrefixesWithSetterPrefixesBuilder withElem(final String elem) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.add(elem);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularWithPrefixesWithSetterPrefixesBuilder withElems(final java.util.Collection<? extends String> elems) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.addAll(elems);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularWithPrefixesWithSetterPrefixesBuilder clearElems() {
      if ((this.elems != null))
          this.elems.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularWithPrefixesWithSetterPrefixes build() {
      java.util.List<String> elems;
      switch (((this.elems == null) ? 0 : this.elems.size())) {
      case 0 :
          elems = java.util.Collections.emptyList();
          break;
      case 1 :
          elems = java.util.Collections.singletonList(this.elems.get(0));
          break;
      default :
          elems = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.elems));
      }
      return new BuilderSingularWithPrefixesWithSetterPrefixes(elems);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderSingularWithPrefixesWithSetterPrefixes.BuilderSingularWithPrefixesWithSetterPrefixesBuilder(elems=" + this.elems) + ")");
    }
  }
  private @Singular java.util.List<String> _elems;
  @java.lang.SuppressWarnings("all") BuilderSingularWithPrefixesWithSetterPrefixes(final java.util.List<String> elems) {
    super();
    this._elems = elems;
  }
  public static @java.lang.SuppressWarnings("all") BuilderSingularWithPrefixesWithSetterPrefixesBuilder builder() {
    return new BuilderSingularWithPrefixesWithSetterPrefixesBuilder();
  }
}
