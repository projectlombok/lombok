class SetterWithoutJavaBeansSpecCapitalization {
  @lombok.Setter int a;
  @lombok.Setter int aField;
	  
  SetterWithoutJavaBeansSpecCapitalization() {
    super();
  }

  public @java.lang.SuppressWarnings("all") void setA(final int a) {
    this.a = a;
  }
	
  public @java.lang.SuppressWarnings("all") void setAField(final int aField) {
    this.aField = aField;
  }
}	

@lombok.experimental.Accessors(javaBeansSpecCapitalization = true) class SetterWithJavaBeansSpecCapitalization {
  @lombok.Setter int a;
  @lombok.Setter int aField;
	  
  SetterWithJavaBeansSpecCapitalization() {
    super();
  }
	  
  public @java.lang.SuppressWarnings("all") void setA(final int a) {
    this.a = a;
  }
	
  public @java.lang.SuppressWarnings("all") void setaField(final int aField) {
    this.aField = aField;
  }
}	
