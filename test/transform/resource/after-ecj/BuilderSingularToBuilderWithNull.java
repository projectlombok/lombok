import lombok.Singular;
@lombok.Builder(toBuilder = true) class BuilderSingularToBuilderWithNull {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularToBuilderWithNullBuilder {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> elems;
    @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullBuilder elem(final String elem) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.add(elem);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullBuilder elems(final java.util.Collection<? extends String> elems) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.addAll(elems);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullBuilder clearElems() {
      if ((this.elems != null))
          this.elems.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNull build() {
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
      return new BuilderSingularToBuilderWithNull(elems);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder(elems=" + this.elems) + ")");
    }
  }
  private @Singular java.util.List<String> elems;
  public static void test() {
    new BuilderSingularToBuilderWithNull(null).toBuilder();
  }
  @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNull(final java.util.List<String> elems) {
    super();
    this.elems = elems;
  }
  public static @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullBuilder builder() {
    return new BuilderSingularToBuilderWithNullBuilder();
  }
  public @java.lang.SuppressWarnings("all") BuilderSingularToBuilderWithNullBuilder toBuilder() {
    final BuilderSingularToBuilderWithNullBuilder builder = new BuilderSingularToBuilderWithNullBuilder();
    if ((this.elems != null))
        builder.elems(this.elems);
    return builder;
  }
}