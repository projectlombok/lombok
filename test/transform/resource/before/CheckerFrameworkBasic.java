//CONF: checkerframework = 3.0
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

@Data @Accessors(chain = true)
class CheckerFrameworkBasic {
	@Wither private final int x;
	private final int y;
	private int z;
}
