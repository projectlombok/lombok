import lombok.Singular;

@lombok.Builder(toBuilder = true, setterPrefix = "with")
class BuilderSingularToBuilderWithNullWithSetterPrefix {
	@Singular private java.util.List<String> elems;
	
	public static void test() {
		new BuilderSingularToBuilderWithNullWithSetterPrefix(null).toBuilder();
	}
}
