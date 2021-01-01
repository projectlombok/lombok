//CONF: lombok.addNullAnnotations = checkerframework
import java.util.List;
import lombok.Singular;

class NullAnnotatedCheckerFrameworkSuperBuilder {
	@lombok.experimental.SuperBuilder
	public static class Parent {
		@lombok.Builder.Default int x = 5;
		int y;
		int z;
		@Singular List<String> names;
	}
	
	@lombok.experimental.SuperBuilder
	public static class ZChild extends Parent {
		@lombok.Builder.Default int a = 1;
		int b;
	}
}
