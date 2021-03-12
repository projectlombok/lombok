import java.util.Set;
@lombok.experimental.SuperBuilder class SuperBuilderSingularCustomized {
  public static abstract class SuperBuilderSingularCustomizedBuilder<C extends SuperBuilderSingularCustomized, B extends SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<C, B>> {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> foos;
    public SuperBuilderSingularCustomizedBuilder() {
      super();
    }
    public B custom(final String value) {
      return self();
    }
    protected abstract @java.lang.SuppressWarnings("all") B self();
    public abstract @java.lang.SuppressWarnings("all") C build();
    public @java.lang.SuppressWarnings("all") B foo(final String foo) {
      if ((this.foos == null))
          this.foos = new java.util.ArrayList<String>();
      this.foos.add(foo);
      return self();
    }
    public @java.lang.SuppressWarnings("all") B foos(final java.util.Collection<? extends String> foos) {
      if ((foos == null))
          {
            throw new java.lang.NullPointerException("foos cannot be null");
          }
      if ((this.foos == null))
          this.foos = new java.util.ArrayList<String>();
      this.foos.addAll(foos);
      return self();
    }
    public @java.lang.SuppressWarnings("all") B clearFoos() {
      if ((this.foos != null))
          this.foos.clear();
      return self();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder(foos=" + this.foos) + ")");
    }
  }
  private static final @java.lang.SuppressWarnings("all") class SuperBuilderSingularCustomizedBuilderImpl extends SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<SuperBuilderSingularCustomized, SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilderImpl> {
    private SuperBuilderSingularCustomizedBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilderImpl self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderSingularCustomized build() {
      return new SuperBuilderSingularCustomized(this);
    }
  }
  private @lombok.Singular Set<String> foos;
  protected @java.lang.SuppressWarnings("all") SuperBuilderSingularCustomized(final SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<?, ?> b) {
    super();
    java.util.Set<String> foos;
    switch (((b.foos == null) ? 0 : b.foos.size())) {
    case 0 :
        foos = java.util.Collections.emptySet();
        break;
    case 1 :
        foos = java.util.Collections.singleton(b.foos.get(0));
        break;
    default :
        foos = new java.util.LinkedHashSet<String>(((b.foos.size() < 0x40000000) ? ((1 + b.foos.size()) + ((b.foos.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
        foos.addAll(b.foos);
        foos = java.util.Collections.unmodifiableSet(foos);
    }
    this.foos = foos;
  }
  public static @java.lang.SuppressWarnings("all") SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<?, ?> builder() {
    return new SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilderImpl();
  }
}