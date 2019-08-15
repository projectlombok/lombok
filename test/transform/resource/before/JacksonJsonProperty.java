import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Builder;
import lombok.Setter;

@Builder
public class JacksonJsonProperty {
	@JsonProperty("kebab-case-prop")
	@JsonSetter(nulls = Nulls.SKIP)
	@Setter
	public String kebabCaseProp;
}
