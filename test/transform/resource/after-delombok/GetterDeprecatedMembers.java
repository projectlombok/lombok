//version 9:
class GetterDeprecatedMembers {
	@Deprecated(since = "1.2", forRemoval = true)
	int both;
	@Deprecated(since = "3")
	int sinceOnly;
	@Deprecated(forRemoval = true)
	int forRemovalOnly;
	@java.lang.Deprecated(since = "1.2", forRemoval = true)
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getBoth() {
		return this.both;
	}
	@java.lang.Deprecated(since = "3")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getSinceOnly() {
		return this.sinceOnly;
	}
	@java.lang.Deprecated(forRemoval = true)
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getForRemovalOnly() {
		return this.forRemovalOnly;
	}
}
