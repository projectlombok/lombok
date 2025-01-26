@lombok.experimental.SuperBuilder class SuperBuilderWithPrefixes {
  public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class SuperBuilderWithPrefixesBuilder<C extends SuperBuilderWithPrefixes, B extends SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilder<C, B>> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int field;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int otherField;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> items;
    public SuperBuilderWithPrefixesBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated B field(final int field) {
      this.field = field;
      return self();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated B otherField(final int otherField) {
      this.otherField = otherField;
      return self();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated B item(final String item) {
      if ((this.items == null))
          this.items = new java.util.ArrayList<String>();
      this.items.add(item);
      return self();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated B items(final java.util.Collection<? extends String> items) {
      if ((items == null))
          {
            throw new java.lang.NullPointerException("items cannot be null");
          }
      if ((this.items == null))
          this.items = new java.util.ArrayList<String>();
      this.items.addAll(items);
      return self();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated B clearItems() {
      if ((this.items != null))
          this.items.clear();
      return self();
    }
    protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
    public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilder(field=" + this.field) + ", otherField=") + this.otherField) + ", items=") + this.items) + ")");
    }
  }
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated class SuperBuilderWithPrefixesBuilderImpl extends SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilder<SuperBuilderWithPrefixes, SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilderImpl> {
    private SuperBuilderWithPrefixesBuilderImpl() {
      super();
    }
    protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilderImpl self() {
      return this;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithPrefixes build() {
      return new SuperBuilderWithPrefixes(this);
    }
  }
  int mField;
  int xOtherField;
  @lombok.Singular java.util.List<String> mItems;
  protected @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithPrefixes(final SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilder<?, ?> b) {
    super();
    this.mField = b.field;
    this.xOtherField = b.otherField;
    java.util.List<String> items;
    switch (((b.items == null) ? 0 : b.items.size())) {
    case 0 :
        items = java.util.Collections.emptyList();
        break;
    case 1 :
        items = java.util.Collections.singletonList(b.items.get(0));
        break;
    default :
        items = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.items));
    }
    this.mItems = items;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilder<?, ?> builder() {
    return new SuperBuilderWithPrefixes.SuperBuilderWithPrefixesBuilderImpl();
  }
}
