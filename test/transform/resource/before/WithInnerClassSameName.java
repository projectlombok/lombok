interface A {
    class Inner<X> {}
}

class WithInnerClassSameName<Z> {
	@lombok.RequiredArgsConstructor
	class Inner<Y> implements A {
		@lombok.With final String x;
	}
}