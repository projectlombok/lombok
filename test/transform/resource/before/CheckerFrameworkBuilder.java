//CONF: checkerframework = true
import java.util.List;
import lombok.Builder;
import lombok.Singular;

@Builder
class CheckerFrameworkBuilder {
	@Builder.Default int x = 5;
	int y;
	int z;
	@Singular List<String> names;
}
