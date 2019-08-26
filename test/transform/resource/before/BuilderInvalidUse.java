//skip compare content
@lombok.Builder
class BuilderInvalidUse {
	private int something;

	@lombok.Getter @lombok.Setter @lombok.experimental.FieldDefaults(makeFinal = true) @lombok.With @lombok.Data @lombok.ToString @lombok.EqualsAndHashCode
	@lombok.AllArgsConstructor
	public static class BuilderInvalidUseBuilder {

	}
}

@lombok.Builder
class AlsoInvalid {
	@lombok.Value
	public static class AlsoInvalidBuilder {

	}
}