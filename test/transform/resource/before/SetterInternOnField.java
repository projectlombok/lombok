import lombok.Setter;

@Setter
public class SetterInternOnField {
	@Setter(internString = true)
    private String str1;
	@Setter(internString = true)
    private java.lang.String str2;
	private String str3;
}
