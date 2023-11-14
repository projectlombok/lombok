import lombok.Setter;
import lombok.Data;

@Data
public class SetterInternWithData {
	@Setter(internString = true)
    private String str1;
}
