//CONF: lombok.noArgsConstructor.extraPrivate = true
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.Builder;

@Builder @Value class ConstructorsWithBuilderDefaults<T> {
	@Builder.Default java.util.List<T> z = new java.util.ArrayList<T>();
	@Builder.Default T x = null;
	T q;
}
