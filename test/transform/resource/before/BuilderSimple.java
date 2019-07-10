import java.util.List;

@lombok.Builder(access = lombok.AccessLevel.PROTECTED)
class BuilderSimple<T> {
	private final int noshow = 0;
	private final int yes;
	private List<T> also;
	private int $butNotMe;
}
