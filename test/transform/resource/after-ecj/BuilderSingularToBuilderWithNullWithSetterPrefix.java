import lombok.Singular;
@lombok.Builder(toBuilder = true,setterPrefix = "with") class BuilderSingularToBuilderWithNullWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSingularToBuilderWithNullWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> elems;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder withElem(final String elem) {
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.add(elem);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder withElems(final java.util.Collection<? extends String> elems) {
      if ((elems == null))
          {
            throw new java.lang.NullPointerException("elems cannot be null");
          }
      if ((this.elems == null))
          this.elems = new java.util.ArrayList<String>();
      this.elems.addAll(elems);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder clearElems() {
      if ((this.elems != null))
          this.elems.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefix build() {
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
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder(elems=" + this.elems) + ")");
    }
  }
  private @Singular java.util.List<String> elems;
  public static void test() {
    new BuilderSingularToBuilderWithNullWithSetterPrefix(null).toBuilder();
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefix(final java.util.List<String> elems) {
    super();
    this.elems = elems;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder builder() {
    return new BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder toBuilder() {
    final BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder builder = new BuilderSingularToBuilderWithNullWithSetterPrefix.BuilderSingularToBuilderWithNullWithSetterPrefixBuilder();
    if ((this.elems != null))
        builder.withElems(this.elems);
    return builder;
  }
}
