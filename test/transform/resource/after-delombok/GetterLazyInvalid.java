class GetterLazyInvalidNotFinal {
	private String fieldName = "";
}
class GetterLazyInvalidNotPrivate {
	final String fieldName = "";
}
class GetterLazyInvalidNotPrivateFinal {
	String fieldName = "";
}
class GetterLazyInvalidNone {
	String fieldName = "";
}
class GetterLazyInvalidClass {
	String fieldName = "";
	@java.lang.SuppressWarnings("all")
	public String getFieldName() {
		return this.fieldName;
	}
}
class GetterLazyInvalidNoInit {
	String fieldName;
}