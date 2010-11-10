import lombok.Setter;
class SetterOnMethod {
	@lombok.Setter(onMethod=@Deprecated) int i;
	@lombok.Setter(onMethod={@java.lang.Deprecated}) int j;
}
@lombok.Setter(onMethod=@Deprecated)
class SetterOnClassOnMethod {
	int i;
	int j;
}
@lombok.Setter(onMethod=@Deprecated)
class SetterOnClassAndOnAField {
	int i;
	@lombok.Setter(onMethod={@java.lang.Deprecated}) int j;
}
