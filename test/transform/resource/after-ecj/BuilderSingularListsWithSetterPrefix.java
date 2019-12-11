import java.util.List;
import java.util.Collection;

import lombok.Singular;
@lombok.Builder(setterPrefix = "with") class BuilderSingularListsWithSetterPrefix<T> {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularListsWithSetterPrefixBuilder<T> {
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<T> children;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<Number> scarves;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<java.lang.Object> rawList;
    @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> withChild(final T child) {
      if ((this.children == null))
          this.children = new java.util.ArrayList<T>();
      this.children.add(child);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> withChildren(final java.util.Collection<? extends T> children) {
      if ((this.children == null))
          this.children = new java.util.ArrayList<T>();
      this.children.addAll(children);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> clearChildren() {
      if ((this.children != null))
          this.children.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> withScarf(final Number scarf) {
      if ((this.scarves == null))
          this.scarves = new java.util.ArrayList<Number>();
      this.scarves.add(scarf);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> withScarves(final java.util.Collection<? extends Number> scarves) {
      if ((this.scarves == null))
          this.scarves = new java.util.ArrayList<Number>();
      this.scarves.addAll(scarves);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> clearScarves() {
      if ((this.scarves != null))
          this.scarves.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> withRawList(final java.lang.Object rawList) {
      if ((this.rawList == null))
          this.rawList = new java.util.ArrayList<java.lang.Object>();
      this.rawList.add(rawList);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> withRawList(final java.util.Collection<?> rawList) {
      if ((this.rawList == null))
          this.rawList = new java.util.ArrayList<java.lang.Object>();
      this.rawList.addAll(rawList);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefixBuilder<T> clearRawList() {
      if ((this.rawList != null))
          this.rawList.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefix<T> build() {
      java.util.List<T> children;
      switch (((this.children == null) ? 0 : this.children.size())) {
      case 0 :
          children = java.util.Collections.emptyList();
          break;
      case 1 :
          children = java.util.Collections.singletonList(this.children.get(0));
          break;
      default :
          children = java.util.Collections.unmodifiableList(new java.util.ArrayList<T>(this.children));
      }
      java.util.Collection<Number> scarves;
      switch (((this.scarves == null) ? 0 : this.scarves.size())) {
      case 0 :
          scarves = java.util.Collections.emptyList();
          break;
      case 1 :
          scarves = java.util.Collections.singletonList(this.scarves.get(0));
          break;
      default :
          scarves = java.util.Collections.unmodifiableList(new java.util.ArrayList<Number>(this.scarves));
      }
      java.util.List<java.lang.Object> rawList;
      switch (((this.rawList == null) ? 0 : this.rawList.size())) {
      case 0 :
          rawList = java.util.Collections.emptyList();
          break;
      case 1 :
          rawList = java.util.Collections.singletonList(this.rawList.get(0));
          break;
      default :
          rawList = java.util.Collections.unmodifiableList(new java.util.ArrayList<java.lang.Object>(this.rawList));
      }
      return new BuilderSingularListsWithSetterPrefix<T>(children, scarves, rawList);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderSingularListsWithSetterPrefix.BuilderSingularListsWithSetterPrefixBuilder(children=" + this.children) + ", scarves=") + this.scarves) + ", rawList=") + this.rawList) + ")");
    }
  }
  private @Singular List<T> children;
  private @Singular Collection<? extends Number> scarves;
  private @SuppressWarnings("all") @Singular("rawList") List rawList;
  @java.lang.SuppressWarnings("all") BuilderSingularListsWithSetterPrefix(final List<T> children, final Collection<? extends Number> scarves, final List rawList) {
    super();
    this.children = children;
    this.scarves = scarves;
    this.rawList = rawList;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderSingularListsWithSetterPrefixBuilder<T> builder() {
    return new BuilderSingularListsWithSetterPrefixBuilder<T>();
  }
}
