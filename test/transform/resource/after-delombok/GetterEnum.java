enum GetterEnum {
	ONE(1, "One");
	private final int id;
	private final String name;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private GetterEnum(final int id, final String name) {
		this.id = id;
		this.name = name;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getId() {
		return this.id;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getName() {
		return this.name;
	}
}
