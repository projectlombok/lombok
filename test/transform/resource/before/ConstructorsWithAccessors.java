@lombok.AllArgsConstructor @lombok.experimental.Accessors(prefix={"p", "_"}) class ConstructorsWithAccessors {
	int plower;
	int pUpper;
	int _huh;
	int __huh2;
}

@lombok.AllArgsConstructor @lombok.experimental.Accessors(prefix={"p", "_"}) class ConstructorsWithAccessorsNonNull {
	@lombok.NonNull Integer plower;
	@lombok.NonNull Integer pUpper;
	@lombok.NonNull Integer _huh;
	@lombok.NonNull final Integer __huh2;
}

