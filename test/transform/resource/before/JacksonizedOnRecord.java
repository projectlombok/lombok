//version 14:
import java.util.List;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.extern.jackson.Jacksonized
@lombok.Builder
@JsonIgnoreProperties
public record JacksonizedOnRecord(@JsonProperty("test") @Nullable String string, @JsonAnySetter @lombok.Singular List<String> values) {
}
