//CONF: lombok.copyJacksonAnnotationsToAccessors = true
//version 8: Jackson deps are at least Java7+.
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data public class JacksonJsonProperty2 {
	@JsonProperty("a") String propertyOne;
	@JsonProperty("b") int propertyTwo;
	Map<String, Object> additional = new HashMap<>();
}
