class WithInnerAnnotation {
  @interface Inner {
    int bar() default 42;
  }
  WithInnerAnnotation() {
    super();
  }
}