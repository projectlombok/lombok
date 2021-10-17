@lombok.Value
@lombok.experimental.Accessors(javaBeansSpecCapitalization = true)
class ValueWithJavaBeansSpecCapitalization {
	final int aField;
}

@lombok.Value
class ValueWithoutJavaBeansSpecCapitalization {
	final int aField;
}
