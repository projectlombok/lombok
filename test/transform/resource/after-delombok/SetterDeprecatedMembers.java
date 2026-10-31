//version 9:
class SetterDeprecatedMembers {
	@Deprecated(since = "1.2", forRemoval = true)
	int both;
	@java.lang.Deprecated(since = "1.2", forRemoval = true)
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setBoth(final int both) {
		this.both = both;
	}
}
