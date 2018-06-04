// CONF: lombok.noArgsConstructor.extraPrivate = true
// CONF: lombok.equalsAndHashCode.callSuper = call
public class PrivateNoArgsConstructor {
	private static class Base {
	}
	
	@lombok.Data
	public static class PrivateNoArgsConstructorNotOnExtends extends Base {
		private final int a;
	}
	
	@lombok.Data
	public static class PrivateNoArgsConstructorOnExtendsObject extends Object {
		private final int b;
	}
	
	@lombok.NoArgsConstructor(force=true)
	@lombok.Data
	@lombok.RequiredArgsConstructor
	public static class PrivateNoArgsConstructorExplicitBefore {
		private final int c;
	}
	
	@lombok.Data
	@lombok.NoArgsConstructor(force=true)
	@lombok.RequiredArgsConstructor
	public static class PrivateNoArgsConstructorExplicitAfter {
		private final int d;
	}
	
	@lombok.Data
	@lombok.NoArgsConstructor(access=lombok.AccessLevel.NONE)
	@lombok.RequiredArgsConstructor
	public static class PrivateNoArgsConstructorExplicitNone {
		private final int e;
	}
	
	@lombok.Data
	public static class PrivateNoArgsConstructorNoFields {
	}
}
