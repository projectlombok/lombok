import java.util.List;
class BuilderSingularNoAutosingularize {
	private List<String> things;
	private List<String> widgets;
	private List<String> items;
	@java.lang.SuppressWarnings("all")
	BuilderSingularNoAutosingularize(final List<String> things, final List<String> widgets, final List<String> items) {
		this.things = things;
		this.widgets = widgets;
		this.items = items;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularNoAutosingularizeBuilder {
		private java.util.ArrayList<String> things;
		private java.util.ArrayList<String> widgets;
		private java.util.ArrayList<String> items;
		@java.lang.SuppressWarnings("all")
		BuilderSingularNoAutosingularizeBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNoAutosingularizeBuilder things(final String things) {
			if (this.things == null) this.things = new java.util.ArrayList<String>();
			this.things.add(things);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNoAutosingularizeBuilder things(final java.util.Collection<? extends String> things) {
			if (this.things == null) this.things = new java.util.ArrayList<String>();
			this.things.addAll(things);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNoAutosingularizeBuilder widget(final String widget) {
			if (this.widgets == null) this.widgets = new java.util.ArrayList<String>();
			this.widgets.add(widget);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNoAutosingularizeBuilder widgets(final java.util.Collection<? extends String> widgets) {
			if (this.widgets == null) this.widgets = new java.util.ArrayList<String>();
			this.widgets.addAll(widgets);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNoAutosingularizeBuilder items(final String items) {
			if (this.items == null) this.items = new java.util.ArrayList<String>();
			this.items.add(items);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNoAutosingularizeBuilder items(final java.util.Collection<? extends String> items) {
			if (this.items == null) this.items = new java.util.ArrayList<String>();
			this.items.addAll(items);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNoAutosingularize build() {
			java.util.List<String> things;
			switch (this.things == null ? 0 : this.things.size()) {
			case 0: 
				things = java.util.Collections.emptyList();
				break;
			case 1: 
				things = java.util.Collections.singletonList(this.things.get(0));
				break;
			default: 
				things = new java.util.ArrayList<String>(this.things.size());
				things.addAll(this.things);
				things = java.util.Collections.unmodifiableList(things);
			}
			java.util.List<String> widgets;
			switch (this.widgets == null ? 0 : this.widgets.size()) {
			case 0: 
				widgets = java.util.Collections.emptyList();
				break;
			case 1: 
				widgets = java.util.Collections.singletonList(this.widgets.get(0));
				break;
			default: 
				widgets = new java.util.ArrayList<String>(this.widgets.size());
				widgets.addAll(this.widgets);
				widgets = java.util.Collections.unmodifiableList(widgets);
			}
			java.util.List<String> items;
			switch (this.items == null ? 0 : this.items.size()) {
			case 0: 
				items = java.util.Collections.emptyList();
				break;
			case 1: 
				items = java.util.Collections.singletonList(this.items.get(0));
				break;
			default: 
				items = new java.util.ArrayList<String>(this.items.size());
				items.addAll(this.items);
				items = java.util.Collections.unmodifiableList(items);
			}
			return new BuilderSingularNoAutosingularize(things, widgets, items);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularNoAutosingularize.BuilderSingularNoAutosingularizeBuilder(things=" + this.things + ", widgets=" + this.widgets + ", items=" + this.items + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderSingularNoAutosingularizeBuilder builder() {
		return new BuilderSingularNoAutosingularizeBuilder();
	}
}