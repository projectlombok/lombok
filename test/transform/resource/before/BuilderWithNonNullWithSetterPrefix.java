@lombok.Builder(setterPrefix = "with")
class BuilderWithNonNullWithSetterPrefix {
	@lombok.NonNull
	private final String id;
}
