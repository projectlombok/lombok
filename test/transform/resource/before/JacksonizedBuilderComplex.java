//CONF: lombok.builder.className = Test*Name
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

class JacksonizedBuilderComplex {
	@Jacksonized
	@Builder(buildMethodName = "execute", setterPrefix = "with")
	private static <T extends Number> void testVoidWithGenerics(T number, int arg2, String arg3, JacksonizedBuilderComplex selfRef) {}
}
