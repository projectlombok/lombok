// Compile with javac, it'll think the T in the generated build() method isn't type compatible.
// Yet, when you take the delomboked output (which delombok will give, but with errors), THAT does compile.

public class I1132RecursiveGenerics {
	public static class Recursive<T extends Recursive<T>> {}
	public static final class Rec extends Recursive<Rec> {}

	@lombok.Builder(builderClassName = "MethodBuilder")
	public static <T extends Recursive<T>> T create() {
		return null;
	}

	public static void main(String[] args) {
		final MethodBuilder<Rec> builder = I1132RecursiveGenerics.builder();
		final Rec rec = builder.build();
//		final Rec rec = I1132RecursiveGenerics.<Rec>builder().build();
	}
}

