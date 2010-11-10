import lombok.Setter;
class SetterOnParam {
	@lombok.Setter(onParam=@SuppressWarnings("all")) int i;
	@lombok.Setter(onParam={@java.lang.SuppressWarnings("all")}) int j;
}
@lombok.Setter(onParam=@SuppressWarnings("all"))
class SetterOnClassOnParam {
	int i;
	int j;
}
@lombok.Setter(onParam=@SuppressWarnings("all"))
class SetterOnClassAndOnAFieldParam {
	int i;
	@lombok.Setter(onParam={@java.lang.SuppressWarnings("all")}) int j;
}
