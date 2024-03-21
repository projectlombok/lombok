//version 8:
//conf: lombok.onX.flagUsage = warning
//skip compare content: We're just checking if the flagUsage key works.
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter(onMethod_ = @Deprecated)
@Setter(onMethod_ = @Deprecated, onParam_ = @Deprecated)
@NoArgsConstructor(onConstructor_ = @Deprecated)
@AllArgsConstructor(onConstructor_ = @Deprecated)
public class OnXFlagUsage {
	private final String a = "";
	private String b;
}

@RequiredArgsConstructor(onConstructor_ = @Deprecated)
class OnXFlagUsage2 {
	private final String a = "";
}