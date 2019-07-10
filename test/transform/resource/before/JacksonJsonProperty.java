import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Setter;

@Builder
public class JacksonJsonProperty {
	@JsonProperty("kebab-case-prop")
	@Setter
	public String kebabCaseProp;
}
