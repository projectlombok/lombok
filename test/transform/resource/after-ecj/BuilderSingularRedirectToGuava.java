import java.util.Set;
import java.util.NavigableMap;
import java.util.Collection;
import lombok.Singular;
@lombok.Builder class BuilderSingularRedirectToGuava {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderSingularRedirectToGuavaBuilder {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableSet.Builder<String> dangerMice;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableSortedMap.Builder<Integer, Number> things;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableList.Builder<Class<?>> doohickeys;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder dangerMouse(String dangerMouse) {
      if ((this.dangerMice == null))
          this.dangerMice = com.google.common.collect.ImmutableSet.builder();
      this.dangerMice.add(dangerMouse);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder dangerMice(java.lang.Iterable<? extends String> dangerMice) {
      if ((this.dangerMice == null))
          this.dangerMice = com.google.common.collect.ImmutableSet.builder();
      this.dangerMice.addAll(dangerMice);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder clearDangerMice() {
      this.dangerMice = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder thing(Integer key, Number value) {
      if ((this.things == null))
          this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
      this.things.put(key, value);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder things(java.util.Map<? extends Integer, ? extends Number> things) {
      if ((this.things == null))
          this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
      this.things.putAll(things);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder clearThings() {
      this.things = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder doohickey(Class<?> doohickey) {
      if ((this.doohickeys == null))
          this.doohickeys = com.google.common.collect.ImmutableList.builder();
      this.doohickeys.add(doohickey);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder doohickeys(java.lang.Iterable<? extends Class<?>> doohickeys) {
      if ((this.doohickeys == null))
          this.doohickeys = com.google.common.collect.ImmutableList.builder();
      this.doohickeys.addAll(doohickeys);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder clearDoohickeys() {
      this.doohickeys = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuava build() {
      java.util.Set<String> dangerMice = ((this.dangerMice == null) ? com.google.common.collect.ImmutableSet.<String>of() : this.dangerMice.build());
      java.util.NavigableMap<Integer, Number> things = ((this.things == null) ? com.google.common.collect.ImmutableSortedMap.<Integer, Number>of() : this.things.build());
      java.util.Collection<Class<?>> doohickeys = ((this.doohickeys == null) ? com.google.common.collect.ImmutableList.<Class<?>>of() : this.doohickeys.build());
      return new BuilderSingularRedirectToGuava(dangerMice, things, doohickeys);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((((("BuilderSingularRedirectToGuava.BuilderSingularRedirectToGuavaBuilder(dangerMice=" + this.dangerMice) + ", things=") + this.things) + ", doohickeys=") + this.doohickeys) + ")");
    }
  }
  private @Singular Set<String> dangerMice;
  private @Singular NavigableMap<Integer, Number> things;
  private @Singular Collection<Class<?>> doohickeys;
  @java.beans.ConstructorProperties({"dangerMice", "things", "doohickeys"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuava(final Set<String> dangerMice, final NavigableMap<Integer, Number> things, final Collection<Class<?>> doohickeys) {
    super();
    this.dangerMice = dangerMice;
    this.things = things;
    this.doohickeys = doohickeys;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularRedirectToGuavaBuilder builder() {
    return new BuilderSingularRedirectToGuavaBuilder();
  }
}