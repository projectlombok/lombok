class GetterLazyTransient {
	@lombok.Getter(lazy=true)
	private final int nonTransientField = 1;
	
	@lombok.Getter(lazy=true)
	private final transient int transientField = 2;
	
	@lombok.Getter
	private final transient int nonLazyTransientField = 3;
}
