//version 17:
//CONF: lombok.jacksonized.jacksonVersion += 2
//CONF: lombok.jacksonized.jacksonVersion += 3
// issue #4051
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

public sealed interface JacksonizedBuilderOnSealedInner permits JacksonizedBuilderOnSealedInner.MyInnerDTO {
	
	@Builder
	@Jacksonized
	final class MyInnerDTO implements JacksonizedBuilderOnSealedInner {
		String field;
	}
}
