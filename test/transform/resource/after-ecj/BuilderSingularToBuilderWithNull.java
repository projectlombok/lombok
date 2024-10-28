import lombok.Singular;
@lombok.Builder(toBuilder = true) class BuilderSingularToBuilderWithNull {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSingularToBuilderWithNullBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> elems;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder elem(final String elem) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.add(elem);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder elems(final java.util.Collection<? extends String> elems) {
      if ((elems == null))
          {
            throw new java.lang.NullPointerException("elems cannot be null");
          }
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.addAll(elems);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder clearElems() {
      if ((this.elems != null))
          this.elems.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNull build() {
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
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder(elems=" + this.elems) + ")");
    }
  }
  private @Singular java.util.List<String> elems;
  public static void test() {
    new BuilderSingularToBuilderWithNull(null).toBuilder();
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNull(final java.util.List<String> elems) {
    super();
    this.elems = elems;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder builder() {
    return new BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder toBuilder() {
    final BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder builder = new BuilderSingularToBuilderWithNull.BuilderSingularToBuilderWithNullBuilder();
    if ((this.elems != null))
        builder.elems(this.elems);
    return builder;
  }
}
