//CONF: lombok.jacksonized.jacksonVersion += 2
//CONF: lombok.jacksonized.jacksonVersion += 3
//version 8: Jackson deps are at least Java7+.
public class JacksonizedSuperBuilderSimple {
	@lombok.extern.jackson.Jacksonized
	@lombok.SuperBuilder
	@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
	public static class Parent {
		int field1;
	}
	
	public static void test() {
		Parent x = Parent.builder().field1(5).build();
	}
}
