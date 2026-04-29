//version 8: Jackson deps are at least Java7+.
//CONF: lombok.jacksonized.jacksonVersion += 3
@lombok.Data
@lombok.experimental.Accessors(fluent = true)
@lombok.extern.jackson.Jacksonized
class JacksonizedAccessorsJackson3 {
	private String name;
	private int age;
	private transient String password;
}