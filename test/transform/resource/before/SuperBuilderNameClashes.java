public class SuperBuilderNameClashes {
	@lombok.experimental.SuperBuilder
	public static class GenericsClash<B, C, C2> {
	}
	
	@lombok.experimental.SuperBuilder
	public static class B {
	}
	
	public static class C2 {
	}
	
	@lombok.experimental.SuperBuilder
	public static class C {
		C2 c2;
	}
}
