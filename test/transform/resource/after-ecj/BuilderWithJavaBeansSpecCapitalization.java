@lombok.Builder(setterPrefix = "set") class BuilderWithJavaBeansSpecCapitalization {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithJavaBeansSpecCapitalizationBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> a;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> aField;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String bField;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalizationBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder setZ(final String z) {
      if ((this.a == null))
          this.a = new java.util.ArrayList<String>();
      this.a.add(z);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder setA(final java.util.Collection<? extends String> a) {
      if ((a == null))
          {
            throw new java.lang.NullPointerException("a cannot be null");
          }
      if ((this.a == null))
          this.a = new java.util.ArrayList<String>();
      this.a.addAll(a);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder clearA() {
      if ((this.a != null))
          this.a.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder setyField(final String yField) {
      if ((this.aField == null))
          this.aField = new java.util.ArrayList<String>();
      this.aField.add(yField);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder setaField(final java.util.Collection<? extends String> aField) {
      if ((aField == null))
          {
            throw new java.lang.NullPointerException("aField cannot be null");
          }
      if ((this.aField == null))
          this.aField = new java.util.ArrayList<String>();
      this.aField.addAll(aField);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder clearaField() {
      if ((this.aField != null))
          this.aField.clear();
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder setbField(final String bField) {
      this.bField = bField;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization build() {
      java.util.List<String> a;
      switch (((this.a == null) ? 0 : this.a.size())) {
      case 0 :
          a = java.util.Collections.emptyList();
          break;
      case 1 :
          a = java.util.Collections.singletonList(this.a.get(0));
          break;
      default :
          a = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.a));
      }
      java.util.List<String> aField;
      switch (((this.aField == null) ? 0 : this.aField.size())) {
      case 0 :
          aField = java.util.Collections.emptyList();
          break;
      case 1 :
          aField = java.util.Collections.singletonList(this.aField.get(0));
          break;
      default :
          aField = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.aField));
      }
      return new BuilderWithJavaBeansSpecCapitalization(a, aField, this.bField);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder(a=" + this.a) + ", aField=") + this.aField) + ", bField=") + this.bField) + ")");
    }
  }
  @lombok.Singular("z") java.util.List<String> a;
  @lombok.Singular("yField") java.util.List<String> aField;
  String bField;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization(final java.util.List<String> a, final java.util.List<String> aField, final String bField) {
    super();
    this.a = a;
    this.aField = aField;
    this.bField = bField;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder builder() {
    return new BuilderWithJavaBeansSpecCapitalization.BuilderWithJavaBeansSpecCapitalizationBuilder();
  }
}
