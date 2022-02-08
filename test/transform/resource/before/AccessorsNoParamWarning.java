@lombok.experimental.Accessors(fluent=true)
class AccessorsNoParams {
	@lombok.Getter @lombok.experimental.Accessors private String otherFieldWithOverride = "";
}

@lombok.experimental.Accessors
class AccessorsNoParams2 {
	@lombok.Setter private boolean foo;
}
