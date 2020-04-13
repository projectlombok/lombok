import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public class JacksonBuilderSingular {
	@JsonAnySetter
	@Singular("any")
	public Map<String, Object> any;

	@JsonProperty("v_a_l_u_e_s")
	@Singular
	public Map<String, Object> values;
}
