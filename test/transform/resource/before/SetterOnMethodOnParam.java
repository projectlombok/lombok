class SetterOnMethodOnParam {
	@lombok.Setter(onMethod=@__(@Deprecated)) int i;
	@lombok.Setter(onMethod=@__({@java.lang.Deprecated, @Test}), onParam=@__(@Test)) int j, k;

	public @interface Test {
	}
}
