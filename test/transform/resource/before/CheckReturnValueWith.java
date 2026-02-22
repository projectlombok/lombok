import lombok.With;
class CheckReturnValueWith {
	@With final int x;
	@With final String name;

	CheckReturnValueWith(int x, String name) {
		this.x = x;
		this.name = name;
	}
}
