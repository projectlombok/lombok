class SetterWithoutJavaBeansSpecCapitalization {
	@lombok.Setter int a;
	@lombok.Setter int aField;
}

@lombok.experimental.Accessors(javaBeansSpecCapitalization=true)
class SetterWithJavaBeansSpecCapitalization {
	@lombok.Setter int a;
	@lombok.Setter int aField;
}

