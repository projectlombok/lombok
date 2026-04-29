//CONF: lombok.checkReturnValueAnnotation = none
import lombok.With;
class CheckReturnValueOff {
	@With final int x;

	CheckReturnValueOff(int x) {
		this.x = x;
	}
}
