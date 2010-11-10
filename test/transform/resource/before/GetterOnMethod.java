import lombok.Getter;
class GetterOnMethod {
	@lombok.Getter(onMethod=@Deprecated) int i;
	@lombok.Getter(onMethod={@java.lang.Deprecated}) int j;
}
@lombok.Getter(onMethod=@Deprecated)
class GetterOnClassOnMethod {
	int i;
	int j;
}
@lombok.Getter(onMethod=@Deprecated)
class GetterOnClassAndOnAField {
	int i;
	@lombok.Getter(onMethod={@java.lang.Deprecated}) int j;
}
