//version 8:
class NonNullOnSetterParameter {
	@lombok.Setter(onParam_ = @lombok.NonNull)
	String[] elements;

	@lombok.Setter(onParam_ = @lombok.NonNull)
	String value;
}
