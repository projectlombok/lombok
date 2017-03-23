import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableTable;
import lombok.Singular;
@lombok.Builder class BuilderSingularGuavaListsSets<T> {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderSingularGuavaListsSetsBuilder<T> {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableList.Builder<T> cards;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableList.Builder<Number> frogs;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableSet.Builder<java.lang.Object> rawSet;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableSortedSet.Builder<String> passes;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") com.google.common.collect.ImmutableTable.Builder<Number, Number, String> users;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> card(T card) {
      if ((this.cards == null))
          this.cards = com.google.common.collect.ImmutableList.builder();
      this.cards.add(card);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> cards(java.lang.Iterable<? extends T> cards) {
      if ((this.cards == null))
          this.cards = com.google.common.collect.ImmutableList.builder();
      this.cards.addAll(cards);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> clearCards() {
      this.cards = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> frog(Number frog) {
      if ((this.frogs == null))
          this.frogs = com.google.common.collect.ImmutableList.builder();
      this.frogs.add(frog);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> frogs(java.lang.Iterable<? extends Number> frogs) {
      if ((this.frogs == null))
          this.frogs = com.google.common.collect.ImmutableList.builder();
      this.frogs.addAll(frogs);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> clearFrogs() {
      this.frogs = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> rawSet(java.lang.Object rawSet) {
      if ((this.rawSet == null))
          this.rawSet = com.google.common.collect.ImmutableSet.builder();
      this.rawSet.add(rawSet);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> rawSet(java.lang.Iterable<?> rawSet) {
      if ((this.rawSet == null))
          this.rawSet = com.google.common.collect.ImmutableSet.builder();
      this.rawSet.addAll(rawSet);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> clearRawSet() {
      this.rawSet = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> pass(String pass) {
      if ((this.passes == null))
          this.passes = com.google.common.collect.ImmutableSortedSet.naturalOrder();
      this.passes.add(pass);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> passes(java.lang.Iterable<? extends String> passes) {
      if ((this.passes == null))
          this.passes = com.google.common.collect.ImmutableSortedSet.naturalOrder();
      this.passes.addAll(passes);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> clearPasses() {
      this.passes = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> user(Number rowKey, Number columnKey, String value) {
      if ((this.users == null))
          this.users = com.google.common.collect.ImmutableTable.builder();
      this.users.put(rowKey, columnKey, value);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> users(com.google.common.collect.Table<? extends Number, ? extends Number, ? extends String> users) {
      if ((this.users == null))
          this.users = com.google.common.collect.ImmutableTable.builder();
      this.users.putAll(users);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSetsBuilder<T> clearUsers() {
      this.users = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSets<T> build() {
      com.google.common.collect.ImmutableList<T> cards = ((this.cards == null) ? com.google.common.collect.ImmutableList.<T>of() : this.cards.build());
      com.google.common.collect.ImmutableCollection<Number> frogs = ((this.frogs == null) ? com.google.common.collect.ImmutableList.<Number>of() : this.frogs.build());
      com.google.common.collect.ImmutableSet<java.lang.Object> rawSet = ((this.rawSet == null) ? com.google.common.collect.ImmutableSet.<java.lang.Object>of() : this.rawSet.build());
      com.google.common.collect.ImmutableSortedSet<String> passes = ((this.passes == null) ? com.google.common.collect.ImmutableSortedSet.<String>of() : this.passes.build());
      com.google.common.collect.ImmutableTable<Number, Number, String> users = ((this.users == null) ? com.google.common.collect.ImmutableTable.<Number, Number, String>of() : this.users.build());
      return new BuilderSingularGuavaListsSets<T>(cards, frogs, rawSet, passes, users);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((((((((("BuilderSingularGuavaListsSets.BuilderSingularGuavaListsSetsBuilder(cards=" + this.cards) + ", frogs=") + this.frogs) + ", rawSet=") + this.rawSet) + ", passes=") + this.passes) + ", users=") + this.users) + ")");
    }
  }
  private @Singular ImmutableList<T> cards;
  private @Singular ImmutableCollection<? extends Number> frogs;
  private @SuppressWarnings("all") @Singular("rawSet") ImmutableSet rawSet;
  private @Singular ImmutableSortedSet<String> passes;
  private @Singular ImmutableTable<? extends Number, ? extends Number, String> users;
  @java.beans.ConstructorProperties({"cards", "frogs", "rawSet", "passes", "users"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderSingularGuavaListsSets(final ImmutableList<T> cards, final ImmutableCollection<? extends Number> frogs, final ImmutableSet rawSet, final ImmutableSortedSet<String> passes, final ImmutableTable<? extends Number, ? extends Number, String> users) {
    super();
    this.cards = cards;
    this.frogs = frogs;
    this.rawSet = rawSet;
    this.passes = passes;
    this.users = users;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <T>BuilderSingularGuavaListsSetsBuilder<T> builder() {
    return new BuilderSingularGuavaListsSetsBuilder<T>();
  }
}
