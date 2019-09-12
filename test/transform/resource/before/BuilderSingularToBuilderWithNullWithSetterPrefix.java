import lombok.Singular;

@lombok.Builder(toBuilder = true, setterPrefix = "with")
class BuilderSingularToBuilderWithNull {
	@Singular private java.util.List<String> elems;
	
	public static void test() {
		new BuilderSingularToBuilderWithNull(null).toBuilder();
	}
}
