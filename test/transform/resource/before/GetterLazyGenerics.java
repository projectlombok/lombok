class GetterLazyGenerics<E> {
	@lombok.Getter(lazy=true)
	private final E field = getAny();
	
	@lombok.Getter(lazy=true)
	private final long field2 = System.currentTimeMillis();
	
	public static <E> E getAny() {
		return null;
	}
}