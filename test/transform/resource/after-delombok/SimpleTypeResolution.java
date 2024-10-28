class SimpleTypeResolutionFail {
	@Getter
	private int x;
}
class SimpleTypeResolutionSuccess {
	private int x;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getX() {
		return this.x;
	}
}
