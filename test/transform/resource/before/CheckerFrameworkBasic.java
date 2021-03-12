//version 8:
//CONF: checkerframework = 4.0
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.With;

@Data @AllArgsConstructor @Accessors(chain = true)
class CheckerFrameworkBasic {
	@With private final int x;
	private final int y;
	private int z;
}
