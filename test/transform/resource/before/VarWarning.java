//CONF: lombok.var.flagUsage = WARNING
import lombok.var;

public class VarWarning {
	public void isOkay() {
		var x = "Warning";
		x.toLowerCase();
	}
}