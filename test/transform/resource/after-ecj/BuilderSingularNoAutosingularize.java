import java.util.List;
import lombok.Singular;
@lombok.Builder class BuilderSingularNoAutosingularize {
  public static @java.lang.SuppressWarnings("all") class BuilderSingularNoAutosingularizeBuilder {
    private java.util.ArrayList<String> things;
    private java.util.ArrayList<String> widgets;
    private java.util.ArrayList<String> items;
    @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder things(String things) {
      if ((this.things == null))
          this.things = new java.util.ArrayList<String>();
      this.things.add(things);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder things(java.util.Collection<? extends String> things) {
      if ((this.things == null))
          this.things = new java.util.ArrayList<String>();
      this.things.addAll(things);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder widget(String widget) {
      if ((this.widgets == null))
          this.widgets = new java.util.ArrayList<String>();
      this.widgets.add(widget);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder widgets(java.util.Collection<? extends String> widgets) {
      if ((this.widgets == null))
          this.widgets = new java.util.ArrayList<String>();
      this.widgets.addAll(widgets);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder items(String items) {
      if ((this.items == null))
          this.items = new java.util.ArrayList<String>();
      this.items.add(items);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder items(java.util.Collection<? extends String> items) {
      if ((this.items == null))
          this.items = new java.util.ArrayList<String>();
      this.items.addAll(items);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularize build() {
      java.util.List<String> things;
      switch (((this.things == null) ? 0 : this.things.size())) {
      case 0 :
          things = java.util.Collections.emptyList();
          break;
      case 1 :
          things = java.util.Collections.singletonList(this.things.get(0));
          break;
      default :
          things = new java.util.ArrayList<String>(this.things.size());
          things.addAll(this.things);
          things = java.util.Collections.unmodifiableList(things);
      }
      java.util.List<String> widgets;
      switch (((this.widgets == null) ? 0 : this.widgets.size())) {
      case 0 :
          widgets = java.util.Collections.emptyList();
          break;
      case 1 :
          widgets = java.util.Collections.singletonList(this.widgets.get(0));
          break;
      default :
          widgets = new java.util.ArrayList<String>(this.widgets.size());
          widgets.addAll(this.widgets);
          widgets = java.util.Collections.unmodifiableList(widgets);
      }
      java.util.List<String> items;
      switch (((this.items == null) ? 0 : this.items.size())) {
      case 0 :
          items = java.util.Collections.emptyList();
          break;
      case 1 :
          items = java.util.Collections.singletonList(this.items.get(0));
          break;
      default :
          items = new java.util.ArrayList<String>(this.items.size());
          items.addAll(this.items);
          items = java.util.Collections.unmodifiableList(items);
      }
      return new BuilderSingularNoAutosingularize(things, widgets, items);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderSingularNoAutosingularize.BuilderSingularNoAutosingularizeBuilder(things=" + this.things) + ", widgets=") + this.widgets) + ", items=") + this.items) + ")");
    }
  }
  private @Singular List<String> things;
  private @Singular("widget") List<String> widgets;
  private @Singular List<String> items;
  @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularize(final List<String> things, final List<String> widgets, final List<String> items) {
    super();
    this.things = things;
    this.widgets = widgets;
    this.items = items;
  }
  public static @java.lang.SuppressWarnings("all") BuilderSingularNoAutosingularizeBuilder builder() {
    return new BuilderSingularNoAutosingularizeBuilder();
  }
}
