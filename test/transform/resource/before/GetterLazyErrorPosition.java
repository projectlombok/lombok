//platform javac: positions in eclipse are hard...
class GetterLazyErrorPosition {
	@lombok.Getter(lazy=true)
	private final String field = 
		true ? 
		"" : 
		new ErrorPosition();
}
