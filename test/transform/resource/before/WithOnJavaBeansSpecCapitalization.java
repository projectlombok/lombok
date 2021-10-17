@lombok.With
@lombok.experimental.Accessors(javaBeansSpecCapitalization = true)
class WithOnJavaBeansSpecCapitalization {
	int aField;
	
	WithOnJavaBeansSpecCapitalization(int aField) {
	}
}

@lombok.With
class WithOffJavaBeansSpecCapitalization {
	int aField;
	
	WithOffJavaBeansSpecCapitalization(int aField) {
	}
}
