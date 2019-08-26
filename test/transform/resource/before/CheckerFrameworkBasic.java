//CONF: checkerframework = 3.0
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.With;

@Data @Accessors(chain = true)
class CheckerFrameworkBasic {
	@With private final int x;
	private final int y;
	private int z;
}
