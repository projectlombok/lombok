import lombok.Builder;
import java.util.*;

@Builder
public class BuilderDefaultsGenerics<N extends Number, T, R extends List<T>> {
	@Builder.Default private java.util.concurrent.Callable<N> callable = null;
	@Builder.Default private T tee = null;
	@Builder.Default private R arrr = null;
}
