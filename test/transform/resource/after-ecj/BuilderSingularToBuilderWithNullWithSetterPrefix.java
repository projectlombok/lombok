import lombok.Singular;
@lombok.Builder(toBuilder = true, setterPrefix = "with") class BuilderSingularToBuilderWithNullWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularToBuilderWithNullWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> elems;
    @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefixBuilder withElem(final String elem) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.add(elem);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefixBuilder withElems(final java.util.Collection<? extends String> elems) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.addAll(elems);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefixBuilder clearElems() {
      if ((this.elems != null))
          this.elems.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefix build() {
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
      return new BuilderSingularToBuilderWithNullWithSetterPrefix(elems);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder(elems=" + this.elems) + ")");
    }
  }
  private @Singular java.util.List<String> elems;
  public static void test() {
    new BuilderSingularToBuilderWithNullWithSetterPrefix(null).toBuilder();
  }
  @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefix(final java.util.List<String> elems) {
    super();
    this.elems = elems;
  }
  public static @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefixBuilder builder() {
    return new BuilderSingularToBuilderWithNullWithSetterPrefixBuilder();
  }
  public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullWithSetterPrefixBuilder toBuilder() {
    final BuilderSingularToBuilderWithNullWithSetterPrefixBuilder builder = new BuilderSingularToBuilderWithNullWithSetterPrefixBuilder();
    if ((this.elems != null))
        builder.elems(this.elems);
    return builder;
  }
}
