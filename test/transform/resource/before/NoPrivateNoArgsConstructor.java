//CONF: lombok.noArgsConstructor.extraPrivate = false
public class NoPrivateNoArgsConstructor {
	@lombok.Data
	public static class NoPrivateNoArgsConstructorData {
		private final int i;
	}

	@lombok.Value
	public static class NoPrivateNoArgsConstructorValue {
		int i;
	}
}
