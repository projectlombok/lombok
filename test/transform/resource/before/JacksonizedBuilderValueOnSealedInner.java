//version 17:
//CONF: lombok.jacksonized.jacksonVersion += 2
//CONF: lombok.jacksonized.jacksonVersion += 3
// issue #4051
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

public sealed interface JacksonizedBuilderValueOnSealedInner permits JacksonizedBuilderValueOnSealedInner.MyInnerDTO {
	
	@Value
	@Builder
	@Jacksonized
	class MyInnerDTO implements JacksonizedBuilderValueOnSealedInner {
		String field;
	}
}
