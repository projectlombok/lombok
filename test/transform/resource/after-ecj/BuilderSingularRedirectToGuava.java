import java.util.Set;
import java.util.NavigableMap;
import java.util.Collection;
import lombok.Singular;
@lombok.Builder class BuilderSingularRedirectToGuava {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSingularRedirectToGuavaBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated com.google.common.collect.ImmutableSet.Builder<String> dangerMice;
    private @java.lang.SuppressWarnings("all") @lombok.Generated com.google.common.collect.ImmutableSortedMap.Builder<Integer, Number> things;
    private @java.lang.SuppressWarnings("all") @lombok.Generated com.google.common.collect.ImmutableList.Builder<Class<?>> doohickeys;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuavaBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder dangerMouse(final String dangerMouse) {
      if ((this.dangerMice == null))
          this.dangerMice = com.google.common.collect.ImmutableSet.builder();
      this.dangerMice.add(dangerMouse);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder dangerMice(final java.lang.Iterable<? extends String> dangerMice) {
      if ((dangerMice == null))
          {
            throw new java.lang.NullPointerException("dangerMice cannot be null");
          }
      if ((this.dangerMice == null))
          this.dangerMice = com.google.common.collect.ImmutableSet.builder();
      this.dangerMice.addAll(dangerMice);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder clearDangerMice() {
      this.dangerMice = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder thing(final Integer key, final Number value) {
      if ((this.things == null))
          this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
      this.things.put(key, value);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder things(final java.util.Map<? extends Integer, ? extends Number> things) {
      if ((things == null))
          {
            throw new java.lang.NullPointerException("things cannot be null");
          }
      if ((this.things == null))
          this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
      this.things.putAll(things);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder clearThings() {
      this.things = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder doohickey(final Class<?> doohickey) {
      if ((this.doohickeys == null))
          this.doohickeys = com.google.common.collect.ImmutableList.builder();
      this.doohickeys.add(doohickey);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder doohickeys(final java.lang.Iterable<? extends Class<?>> doohickeys) {
      if ((doohickeys == null))
          {
            throw new java.lang.NullPointerException("doohickeys cannot be null");
          }
      if ((this.doohickeys == null))
          this.doohickeys = com.google.common.collect.ImmutableList.builder();
      this.doohickeys.addAll(doohickeys);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder clearDoohickeys() {
      this.doohickeys = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava build() {
      java.util.Set<String> dangerMice = ((this.dangerMice == null) ? com.google.common.collect.ImmutableSet.<String>of() : this.dangerMice.build());
      java.util.NavigableMap<Integer, Number> things = ((this.things == null) ? com.google.common.collect.ImmutableSortedMap.<Integer, Number>of() : this.things.build());
      java.util.Collection<Class<?>> doohickeys = ((this.doohickeys == null) ? com.google.common.collect.ImmutableList.<Class<?>>of() : this.doohickeys.build());
      return new BuilderSingularRedirectToGuava(dangerMice, things, doohickeys);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder(dangerMice=" + this.dangerMice) + ", things=") + this.things) + ", doohickeys=") + this.doohickeys) + ")");
    }
  }
  private @Singular Set<String> dangerMice;
  private @Singular NavigableMap<Integer, Number> things;
  private @Singular Collection<Class<?>> doohickeys;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava(final Set<String> dangerMice, final NavigableMap<Integer, Number> things, final Collection<Class<?>> doohickeys) {
    super();
    this.dangerMice = dangerMice;
    this.things = things;
    this.doohickeys = doohickeys;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder builder() {
    return new BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder();
  }
}
