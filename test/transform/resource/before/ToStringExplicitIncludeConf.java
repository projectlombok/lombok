//CONF: lombok.toString.onlyExplicitlyIncluded = true

@lombok.ToString
class ToStringExplicitIncludeConf {
	int x;
	@lombok.ToString.Include int y;
}
