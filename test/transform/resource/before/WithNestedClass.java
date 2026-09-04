public class WithNestedClass<Z> {
	@lombok.RequiredArgsConstructor
	static class Inner<Z> {
		@lombok.With final String x;
	}
}

