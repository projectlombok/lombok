@lombok.Builder(setterPrefix = "with")
class BuilderWithNonNull {
	@lombok.NonNull
	private final String id;
}
