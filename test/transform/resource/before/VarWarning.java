//CONF: lombok.var.flagUsage = WARNING
//skip compare contents
import lombok.experimental.var;

public class VarWarning {
	public void isOkay() {
		var x = "Warning";
		x.toLowerCase();
	}
}