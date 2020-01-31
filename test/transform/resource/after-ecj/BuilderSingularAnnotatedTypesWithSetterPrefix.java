import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.Map;
import lombok.NonNull;
import lombok.Singular;
@Target(ElementType.TYPE_USE) @interface MyAnnotation {
}
@lombok.Builder(setterPrefix = "with") class BuilderSingularAnnotatedTypesWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularAnnotatedTypesWithSetterPrefixBuilder {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<@MyAnnotation @NonNull String> foos;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<@MyAnnotation @NonNull String> bars$key;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<@MyAnnotation @NonNull Integer> bars$value;
    @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder withFoo(final @MyAnnotation @NonNull String foo) {
      if ((foo == null))
          {
            throw new java.lang.NullPointerException("foo is marked non-null but is null");
          }
      if ((this.foos == null))
          this.foos = new java.util.ArrayList<@MyAnnotation @NonNull String>();
      this.foos.add(foo);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder withFoos(final java.util.Collection<? extends @MyAnnotation @NonNull String> foos) {
      if ((foos == null))
          {
            throw new java.lang.NullPointerException("foos cannot be null");
          }
      if ((this.foos == null))
          this.foos = new java.util.ArrayList<@MyAnnotation @NonNull String>();
      this.foos.addAll(foos);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder clearFoos() {
      if ((this.foos != null))
          this.foos.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder withBar(final @MyAnnotation @NonNull String barKey, final @MyAnnotation @NonNull Integer barValue) {
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
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder withBars(final java.util.Map<? extends @MyAnnotation @NonNull String, ? extends @MyAnnotation @NonNull Integer> bars) {
      if ((bars == null))
          {
            throw new java.lang.NullPointerException("bars cannot be null");
          }
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
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder clearBars() {
      if ((this.bars$key != null))
          {
            this.bars$key.clear();
            this.bars$value.clear();
          }
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix build() {
      java.util.Set<@MyAnnotation @NonNull String> foos;
      switch (((this.foos == null) ? 0 : this.foos.size())) {
      case 0 :
          foos = java.util.Collections.emptySet();
          break;
      case 1 :
          foos = java.util.Collections.singleton(this.foos.get(0));
          break;
      default :
          foos = new java.util.LinkedHashSet<@MyAnnotation @NonNull String>(((this.foos.size() < 0x40000000) ? ((1 + this.foos.size()) + ((this.foos.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
          foos.addAll(this.foos);
          foos = java.util.Collections.unmodifiableSet(foos);
      }
      java.util.Map<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer> bars;
      switch (((this.bars$key == null) ? 0 : this.bars$key.size())) {
      case 0 :
          bars = java.util.Collections.emptyMap();
          break;
      case 1 :
          bars = java.util.Collections.singletonMap(this.bars$key.get(0), this.bars$value.get(0));
          break;
      default :
          bars = new java.util.LinkedHashMap<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer>(((this.bars$key.size() < 0x40000000) ? ((1 + this.bars$key.size()) + ((this.bars$key.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
          for (int $i = 0;; ($i < this.bars$key.size()); $i ++) 
            bars.put(this.bars$key.get($i), this.bars$value.get($i));
          bars = java.util.Collections.unmodifiableMap(bars);
      }
      return new BuilderSingularAnnotatedTypesWithSetterPrefix(foos, bars);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder(foos=" + this.foos) + ", bars$key=") + this.bars$key) + ", bars$value=") + this.bars$value) + ")");
    }
  }
  private @Singular Set<@MyAnnotation @NonNull String> foos;
  private @Singular Map<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer> bars;
  @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix(final Set<@MyAnnotation @NonNull String> foos, final Map<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer> bars) {
    super();
    this.foos = foos;
    this.bars = bars;
  }
  public static @java.lang.SuppressWarnings("all") BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder builder() {
    return new BuilderSingularAnnotatedTypesWithSetterPrefix.BuilderSingularAnnotatedTypesWithSetterPrefixBuilder();
  }
}
