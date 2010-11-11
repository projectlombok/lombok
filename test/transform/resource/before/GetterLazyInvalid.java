class GetterLazyInvalidNotFinal {
	@lombok.Getter(lazy=true)
	private String fieldName = "";
}
class GetterLazyInvalidNotPrivate {
	@lombok.Getter(lazy=true)
	final String fieldName = "";
}
class GetterLazyInvalidNotPrivateFinal {
	@lombok.Getter(lazy=true)
	String fieldName = "";
}
class GetterLazyInvalidNone {
	@lombok.Getter(lazy=true, value=lombok.AccessLevel.NONE)
	String fieldName = "";
}
@lombok.Getter(lazy = true)
class GetterLazyInvalidClass {
	String fieldName = "";
}
class GetterLazyInvalidNoInit {
	@lombok.Getter(lazy = true)
	String fieldName;
}
