class SuperBuilderWithCustomBuilderClassName {
  static @lombok.experimental.SuperBuilder class SuperClass {
    public static abstract @java.lang.SuppressWarnings("all") class Builder<C extends SuperBuilderWithCustomBuilderClassName.SuperClass, B extends SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<C, B>> {
      public Builder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return "SuperBuilderWithCustomBuilderClassName.SuperClass.Builder()";
      }
    }
    private static final @java.lang.SuppressWarnings("all") class BuilderImpl extends SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<SuperBuilderWithCustomBuilderClassName.SuperClass, SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl> {
      private BuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithCustomBuilderClassName.SuperClass build() {
        return new SuperBuilderWithCustomBuilderClassName.SuperClass(this);
      }
    }
    protected @java.lang.SuppressWarnings("all") SuperClass(final SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<?, ?> b) {
      super();
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<?, ?> builder() {
      return new SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl();
    }
  }
  static @lombok.experimental.SuperBuilder class SubClass extends SuperClass {
    public static abstract @java.lang.SuppressWarnings("all") class Builder<C extends SuperBuilderWithCustomBuilderClassName.SubClass, B extends SuperBuilderWithCustomBuilderClassName.SubClass.Builder<C, B>> extends SuperClass.Builder<C, B> {
      public Builder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderWithCustomBuilderClassName.SubClass.Builder(super=" + super.toString()) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class BuilderImpl extends SuperBuilderWithCustomBuilderClassName.SubClass.Builder<SuperBuilderWithCustomBuilderClassName.SubClass, SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl> {
      private BuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithCustomBuilderClassName.SubClass build() {
        return new SuperBuilderWithCustomBuilderClassName.SubClass(this);
      }
    }
    protected @java.lang.SuppressWarnings("all") SubClass(final SuperBuilderWithCustomBuilderClassName.SubClass.Builder<?, ?> b) {
      super(b);
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderWithCustomBuilderClassName.SubClass.Builder<?, ?> builder() {
      return new SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl();
    }
  }
  SuperBuilderWithCustomBuilderClassName() {
    super();
  }
}