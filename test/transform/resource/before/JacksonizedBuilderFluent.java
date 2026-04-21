//CONF: lombok.jacksonized.jacksonVersion += 2
//CONF: lombok.jacksonized.jacksonVersion += 3
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.extern.jackson.Jacksonized
@lombok.experimental.Accessors(fluent=true)
@lombok.Builder
public class JacksonizedBuilderFluent {
	private String fieldName1;
	@JsonProperty("FieldName2")
	private String fieldName2;
	private String fieldName3;
}
