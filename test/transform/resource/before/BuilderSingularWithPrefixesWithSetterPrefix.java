import lombok.Singular;

@lombok.Builder(setterPrefix = "with")
@lombok.experimental.Accessors(prefix = "_")
class BuilderSingularWithPrefixesWithSetterPrefix {
	@Singular private java.util.List<String> _elems;
}
