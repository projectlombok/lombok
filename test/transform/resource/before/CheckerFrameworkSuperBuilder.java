//CONF: checkerframework = true
import java.util.List;
import lombok.Singular;

class CheckerFrameworkSuperBuilder {
	@lombok.experimental.SuperBuilder
	public static class Parent {
		@lombok.Builder.Default int x = 5;
		int y;
		int z;
		@Singular List<String> names;
	}
	
	@lombok.experimental.SuperBuilder
	public static class Child extends Parent {
		@lombok.Builder.Default int a = 1;
		int b;
	}
}
