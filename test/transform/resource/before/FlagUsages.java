//skip compare content
//CONF: lombok.Getter.flagUsage = WARNING
//CONF: lombok.experimental.flagUsage = ERROR
public class FlagUsages {
	@lombok.Getter String x;

	@lombok.experimental.Wither String z;

	public FlagUsages(String x, String y) {
	}
}
