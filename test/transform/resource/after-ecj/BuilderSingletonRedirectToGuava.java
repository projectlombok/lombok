import java.util.Set;
import java.util.NavigableMap;
import java.util.Collection;
import lombok.Singular;
@lombok.Builder class BuilderSingletonRedirectToGuava {
  public static @java.lang.SuppressWarnings("all") class BuilderSingletonRedirectToGuavaBuilder {
    private com.google.common.collect.ImmutableSet.Builder<String> dangerMice;
    private com.google.common.collect.ImmutableSortedMap.Builder<Integer, Number> things;
    private com.google.common.collect.ImmutableList.Builder<Class<?>> doohickeys;
    @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder dangerMouse(String dangerMouse) {
      if ((this.dangerMice == null))
          this.dangerMice = com.google.common.collect.ImmutableSet.builder();
      this.dangerMice.add(dangerMouse);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder dangerMice(java.lang.Iterable<? extends String> dangerMice) {
      if ((this.dangerMice == null))
          this.dangerMice = com.google.common.collect.ImmutableSet.builder();
      this.dangerMice.addAll(dangerMice);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder thing(Integer thing$key, Number thing$value) {
      if ((this.things == null))
          this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
      this.things.put(thing$key, thing$value);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder things(java.util.Map<? extends Integer, ? extends Number> things) {
      if ((this.things == null))
          this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
      this.things.putAll(things);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder doohickey(Class<?> doohickey) {
      if ((this.doohickeys == null))
          this.doohickeys = com.google.common.collect.ImmutableList.builder();
      this.doohickeys.add(doohickey);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder doohickeys(java.lang.Iterable<? extends Class<?>> doohickeys) {
      if ((this.doohickeys == null))
          this.doohickeys = com.google.common.collect.ImmutableList.builder();
      this.doohickeys.addAll(doohickeys);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuava build() {
      java.util.Set<String> dangerMice = ((this.dangerMice == null) ? com.google.common.collect.ImmutableSet.of() : this.dangerMice.build());
      java.util.NavigableMap<Integer, Number> things = ((this.things == null) ? com.google.common.collect.ImmutableSortedMap.of() : this.things.build());
      java.util.Collection<Class<?>> doohickeys = ((this.doohickeys == null) ? com.google.common.collect.ImmutableList.of() : this.doohickeys.build());
      return new BuilderSingletonRedirectToGuava(dangerMice, things, doohickeys);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderSingletonRedirectToGuava.BuilderSingletonRedirectToGuavaBuilder(dangerMice=" + this.dangerMice) + ", things=") + this.things) + ", doohickeys=") + this.doohickeys) + ")");
    }
  }
  private @Singular Set<String> dangerMice;
  private @Singular NavigableMap<Integer, Number> things;
  private @Singular Collection<Class<?>> doohickeys;
  @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuava(final Set<String> dangerMice, final NavigableMap<Integer, Number> things, final Collection<Class<?>> doohickeys) {
    super();
    this.dangerMice = dangerMice;
    this.things = things;
    this.doohickeys = doohickeys;
  }
  public static @java.lang.SuppressWarnings("all") BuilderSingletonRedirectToGuavaBuilder builder() {
    return new BuilderSingletonRedirectToGuavaBuilder();
  }
}