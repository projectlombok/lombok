import lombok.experimental.Value;
@lombok.experimental.Value class Value1 {
	final int x;
	String name;
}
@Value @lombok.experimental.NonFinal class Value2 {
	public int x;
	String name;
}
@Value class Value3 {
	@lombok.experimental.NonFinal int x;
	int y;
}