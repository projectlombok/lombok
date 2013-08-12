class GetterOnMethod {
	@lombok.Getter(onMethod=@__(@Deprecated)) int i;
	@lombok.Getter(onMethod=@__({@java.lang.Deprecated, @Test})) int j, k;

	public @interface Test {
	}
}
