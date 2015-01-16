import java.util.List;
import java.util.Collection;

import lombok.Singular;
@lombok.Builder class BuilderSingletonLists<T> {
  public static @java.lang.SuppressWarnings("all") class BuilderSingletonListsBuilder<T> {
    private java.util.ArrayList<T> children;
    private java.util.ArrayList<Number> scarves;
    private java.util.ArrayList<java.lang.Object> rawList;
    @java.lang.SuppressWarnings("all") BuilderSingletonListsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonListsBuilder<T> child(T child) {
      if ((this.children == null))
          this.children = new java.util.ArrayList<T>();
      this.children.add(child);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonListsBuilder<T> children(java.util.Collection<? extends T> children) {
      if ((this.children == null))
          this.children = new java.util.ArrayList<T>();
      this.children.addAll(children);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonListsBuilder<T> scarf(Number scarf) {
      if ((this.scarves == null))
          this.scarves = new java.util.ArrayList<Number>();
      this.scarves.add(scarf);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonListsBuilder<T> scarves(java.util.Collection<? extends Number> scarves) {
      if ((this.scarves == null))
          this.scarves = new java.util.ArrayList<Number>();
      this.scarves.addAll(scarves);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonListsBuilder<T> rawList(java.lang.Object rawList) {
      if ((this.rawList == null))
          this.rawList = new java.util.ArrayList<java.lang.Object>();
      this.rawList.add(rawList);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonListsBuilder<T> rawList(java.util.Collection<?> rawList) {
      if ((this.rawList == null))
          this.rawList = new java.util.ArrayList<java.lang.Object>();
      this.rawList.addAll(rawList);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingletonLists<T> build() {
      java.util.List<T> children;
      switch (((this.children == null) ? 0 : this.children.size())) {
      case 0 :
          children = java.util.Collections.emptyList();
          break;
      case 1 :
          children = java.util.Collections.singletonList(this.children.get(0));
          break;
      default :
          children = new java.util.ArrayList<T>(this.children.size());
          children.addAll(this.children);
          children = java.util.Collections.unmodifiableList(children);
      }
      java.util.List<Number> scarves;
      switch (((this.scarves == null) ? 0 : this.scarves.size())) {
      case 0 :
          scarves = java.util.Collections.emptyList();
          break;
      case 1 :
          scarves = java.util.Collections.singletonList(this.scarves.get(0));
          break;
      default :
          scarves = new java.util.ArrayList<Number>(this.scarves.size());
          scarves.addAll(this.scarves);
          scarves = java.util.Collections.unmodifiableList(scarves);
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
          rawList = new java.util.ArrayList<java.lang.Object>(this.rawList.size());
          rawList.addAll(this.rawList);
          rawList = java.util.Collections.unmodifiableList(rawList);
      }
      return new BuilderSingletonLists<T>(children, scarves, rawList);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderSingletonLists.BuilderSingletonListsBuilder(children=" + this.children) + ", scarves=") + this.scarves) + ", rawList=") + this.rawList) + ")");
    }
  }
  private @Singular List<T> children;
  private @Singular Collection<? extends Number> scarves;
  private @SuppressWarnings("all") @Singular("rawList") List rawList;
  @java.lang.SuppressWarnings("all") BuilderSingletonLists(final List<T> children, final Collection<? extends Number> scarves, final List rawList) {
    super();
    this.children = children;
    this.scarves = scarves;
    this.rawList = rawList;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderSingletonListsBuilder<T> builder() {
    return new BuilderSingletonListsBuilder<T>();
  }
}