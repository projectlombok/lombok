@lombok.EqualsAndHashCode(doNotUseGetters = true)
@lombok.ToString(doNotUseGetters = true)
class GetterLazyEahcToString {
	@lombok.Getter(lazy=true)
	private final String value = "";
}
