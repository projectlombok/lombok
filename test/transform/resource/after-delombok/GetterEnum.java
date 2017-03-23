enum GetterEnum {
	ONE(1, "One");
	private final int id;
	private final String name;
	@java.beans.ConstructorProperties({"id", "name"})
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	private GetterEnum(final int id, final String name) {
		this.id = id;
		this.name = name;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int getId() {
		return this.id;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public String getName() {
		return this.name;
	}
}
