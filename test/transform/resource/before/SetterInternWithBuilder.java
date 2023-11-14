import lombok.Setter;
import lombok.Builder;

@Builder
public class SetterInternWithBuilder {
	@Setter(internString = true)
    private String str1;
}
