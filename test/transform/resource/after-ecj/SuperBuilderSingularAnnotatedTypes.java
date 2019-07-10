import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.Map;
import lombok.NonNull;
import lombok.Singular;
@Target(ElementType.TYPE_USE) @interface MyAnnotation {
}
@lombok.experimental.SuperBuilder class SuperBuilderSingularAnnotatedTypes {
  public static abstract @java.lang.SuppressWarnings("all") class SuperBuilderSingularAnnotatedTypesBuilder<C extends SuperBuilderSingularAnnotatedTypes, B extends SuperBuilderSingularAnnotatedTypesBuilder<C, B>> {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<@MyAnnotation @NonNull String> foos;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<@MyAnnotation @NonNull String> bars$key;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<@MyAnnotation @NonNull Integer> bars$value;
    public SuperBuilderSingularAnnotatedTypesBuilder() {
      super();
    }
    protected abstract @java.lang.SuppressWarnings("all") B self();
    public abstract @java.lang.SuppressWarnings("all") C build();
    public @java.lang.SuppressWarnings("all") B foo(final @MyAnnotation @NonNull String foo) {
      if ((foo == null))
          {
            throw new java.lang.NullPointerException("foo is marked non-null but is null");
          }
      if ((this.foos == null))
          this.foos = new java.util.ArrayList<@MyAnnotation @NonNull String>();
      this.foos.add(foo);
      return self();
    }
    public @java.lang.SuppressWarnings("all") B foos(final java.util.Collection<? extends @MyAnnotation @NonNull String> foos) {
      if ((this.foos == null))
          this.foos = new java.util.ArrayList<@MyAnnotation @NonNull String>();
      this.foos.addAll(foos);
      return self();
    }
    public @java.lang.SuppressWarnings("all") B clearFoos() {
      if ((this.foos != null))
          this.foos.clear();
      return self();
    }
    public @java.lang.SuppressWarnings("all") B bar(final @MyAnnotation @NonNull String barKey, final @MyAnnotation @NonNull Integer barValue) {
      if ((barKey == null))
          {
            throw new java.lang.NullPointerException("barKey is marked non-null but is null");
          }
      if ((barValue == null))
          {
            throw new java.lang.NullPointerException("barValue is marked non-null but is null");
          }
      if ((this.bars$key == null))
          {
            this.bars$key = new java.util.ArrayList<@MyAnnotation @NonNull String>();
            this.bars$value = new java.util.ArrayList<@MyAnnotation @NonNull Integer>();
          }
      this.bars$key.add(barKey);
      this.bars$value.add(barValue);
      return self();
    }
    public @java.lang.SuppressWarnings("all") B bars(final java.util.Map<? extends @MyAnnotation @NonNull String, ? extends @MyAnnotation @NonNull Integer> bars) {
      if ((this.bars$key == null))
          {
            this.bars$key = new java.util.ArrayList<@MyAnnotation @NonNull String>();
            this.bars$value = new java.util.ArrayList<@MyAnnotation @NonNull Integer>();
          }
      for (java.util.Map.Entry<? extends @MyAnnotation @NonNull String, ? extends @MyAnnotation @NonNull Integer> $lombokEntry : bars.entrySet()) 
        {
          this.bars$key.add($lombokEntry.getKey());
          this.bars$value.add($lombokEntry.getValue());
        }
      return self();
    }
    public @java.lang.SuppressWarnings("all") B clearBars() {
      if ((this.bars$key != null))
          {
            this.bars$key.clear();
            this.bars$value.clear();
          }
      return self();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("SuperBuilderSingularAnnotatedTypes.SuperBuilderSingularAnnotatedTypesBuilder(foos=" + this.foos) + ", bars$key=") + this.bars$key) + ", bars$value=") + this.bars$value) + ")");
    }
  }
  private static final @java.lang.SuppressWarnings("all") class SuperBuilderSingularAnnotatedTypesBuilderImpl extends SuperBuilderSingularAnnotatedTypesBuilder<SuperBuilderSingularAnnotatedTypes, SuperBuilderSingularAnnotatedTypesBuilderImpl> {
    private SuperBuilderSingularAnnotatedTypesBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderSingularAnnotatedTypesBuilderImpl self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderSingularAnnotatedTypes build() {
      return new SuperBuilderSingularAnnotatedTypes(this);
    }
  }
  private @Singular Set<@MyAnnotation @NonNull String> foos;
  private @Singular Map<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer> bars;
  protected @java.lang.SuppressWarnings("all") SuperBuilderSingularAnnotatedTypes(final SuperBuilderSingularAnnotatedTypesBuilder<?, ?> b) {
    super();
    java.util.Set<@MyAnnotation @NonNull String> foos;
    switch (((b.foos == null) ? 0 : b.foos.size())) {
    case 0 :
        foos = java.util.Collections.emptySet();
        break;
    case 1 :
        foos = java.util.Collections.singleton(b.foos.get(0));
        break;
    default :
        foos = new java.util.LinkedHashSet<@MyAnnotation @NonNull String>(((b.foos.size() < 0x40000000) ? ((1 + b.foos.size()) + ((b.foos.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
        foos.addAll(b.foos);
        foos = java.util.Collections.unmodifiableSet(foos);
    }
    this.foos = foos;
    java.util.Map<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer> bars;
    switch (((b.bars$key == null) ? 0 : b.bars$key.size())) {
    case 0 :
        bars = java.util.Collections.emptyMap();
        break;
    case 1 :
        bars = java.util.Collections.singletonMap(b.bars$key.get(0), b.bars$value.get(0));
        break;
    default :
        bars = new java.util.LinkedHashMap<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer>(((b.bars$key.size() < 0x40000000) ? ((1 + b.bars$key.size()) + ((b.bars$key.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
        for (int $i = 0;; ($i < b.bars$key.size()); $i ++) 
          bars.put(b.bars$key.get($i), b.bars$value.get($i));
        bars = java.util.Collections.unmodifiableMap(bars);
    }
    this.bars = bars;
  }
  public static @java.lang.SuppressWarnings("all") SuperBuilderSingularAnnotatedTypesBuilder<?, ?> builder() {
    return new SuperBuilderSingularAnnotatedTypesBuilderImpl();
  }
}