//CONF: lombok.var.flagUsage = ALLOW
import lombok.experimental.var;

class VarNullInit {
	void method() {
		var x = null;
	}
}