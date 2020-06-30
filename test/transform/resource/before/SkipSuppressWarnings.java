//CONF: lombok.addSuppressWarnings = false

class SkipSuppressWarnings {
	@lombok.Getter
	private String field = "";
	
	@lombok.Getter(lazy=true)
	private final String field2 = "";
}