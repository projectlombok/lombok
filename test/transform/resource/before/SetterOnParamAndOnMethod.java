class SetterOnParamAndOnMethod {
	@lombok.Setter(onMethod=@Deprecated,onParam=@SuppressWarnings("all")) int i;
}
