//CONF: lombok.var.flagUsage = ALLOW
import lombok.experimental.var;

public class VarInForOld {
	public void oldFor() {
		for (var i = 0; i < 100; ++i) {
			System.out.println(i);
		}
	}
}