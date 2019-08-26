//skip compare content
//CONF: lombok.Getter.flagUsage = WARNING
//CONF: lombok.experimental.flagUsage = ERROR
@lombok.experimental.FieldNameConstants
public class FlagUsages {
	@lombok.Getter String x;

	String z;

	public FlagUsages(String x, String y) {
	}
}
