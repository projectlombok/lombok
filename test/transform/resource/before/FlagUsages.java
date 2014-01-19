//conf: lombok.Getter.flagUsage = WARNING
//conf: lombok.experimental.flagUsage = ERROR
public class FlagUsages {
	@lombok.Getter String x;
	
	@lombok.experimental.Wither String z;

	public FlagUsages(String x, String y) {
	}
}
