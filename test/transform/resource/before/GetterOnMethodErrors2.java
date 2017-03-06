//version 8:
class GetterOnMethodErrors2 {
	@lombok.Getter(onMethod=@_A_(@Deprecated)) private int bad1;
	@lombok.Getter(onMethod=@__(5)) private int bad2;
	@lombok.Getter(onMethod=@__({@Deprecated, 5})) private int bad3;
	@lombok.Getter(onMethod=@$(bar=@Deprecated)) private int bad4;
	@lombok.Getter(onMethod=@__) private int good1;
	@lombok.Getter(onMethod=@X()) private int good2;
	@lombok.Getter(onMethod=@__(value=@Deprecated)) private int good3;
	@lombok.Getter(onMethod=@xXx$$(value={@Deprecated, @Test})) private int good4;
	public @interface Test {
	}
}
