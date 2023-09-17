// version 8:
class GetterLazyArguments {
	static String fun() { return null; }
	static String stringInt(String arg1, Integer arg2) { return null; }
	static String stringRunnable(String arg1, Runnable arg2) { return null; }
	
	@lombok.Getter(lazy=true)
	private final String field1 = stringInt(("a"), (1));
	
	@lombok.Getter(lazy=true)
	private final String field2 = stringInt(true ? "a" : "b", true ? 1 : 0);
	
	@lombok.Getter(lazy=true)
	private final String field3 = stringInt(("a"), true ? 1 : 0);
	
	@lombok.Getter(lazy=true)
	private final String field4 = stringRunnable(fun(), () -> { });
	
	@lombok.Getter(lazy=true)
	private final String field5 = stringRunnable(("a"), () -> { });
	
	@lombok.Getter(lazy=true)
	private final String field6 = true ? stringInt(true ? "a" : "b", true ? 1 : 0) : "";
}
