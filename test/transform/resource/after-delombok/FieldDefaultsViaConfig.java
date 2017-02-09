class FieldDefaultsViaConfig1 {
	private final int x;
	private int y;
	FieldDefaultsViaConfig1(int x) {
		this.x = x;
	}
}
class FieldDefaultsViaConfig2 {
	final int x = 2;
	protected final int y = 0;
}
