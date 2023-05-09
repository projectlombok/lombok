//version 8: Jackson deps are at least Java7+.

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public class JacksonBuilderSimpleWithJsonUnwrapped {

	public static class Embedded {
		private int value;
	}

	@JsonUnwrapped
	private Embedded embedded;

}
