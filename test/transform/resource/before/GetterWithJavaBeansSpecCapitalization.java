class GetterWithoutJavaBeansSpecCapitalization {
	@lombok.Getter int a;
	@lombok.Getter int aField;
}

@lombok.experimental.Accessors(javaBeansSpecCapitalization=true)
class GetterWithJavaBeansSpecCapitalization {
	@lombok.Getter int a;
	@lombok.Getter int aField;
}

