import lombok.experimental.FieldNameConstants;
@FieldNameConstants class FieldNameConstantsJavadoc1 {
  public static final @java.lang.SuppressWarnings("all") @lombok.Generated class Fields {
    public static final java.lang.String withJavadoc = "withJavadoc";
    public static final java.lang.String withoutJavadoc = "withoutJavadoc";
    public static final java.lang.String withTags = "withTags";
    public static final java.lang.String withSections = "withSections";
    <clinit>() {
    }
    private @java.lang.SuppressWarnings("all") @lombok.Generated Fields() {
      super();
    }
  }
  String withJavadoc;
  String withoutJavadoc;
  String withTags;
  @lombok.Getter String withSections;
  FieldNameConstantsJavadoc1() {
    super();
  }
  /**
   * Getter section
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getWithSections() {
    return this.withSections;
  }
}
@FieldNameConstants(asEnum = true) class FieldNameConstantsJavadoc2 {
  public @java.lang.SuppressWarnings("all") @lombok.Generated enum Fields {
    withJavadoc(),
    withoutJavadoc(),
    private @java.lang.SuppressWarnings("all") @lombok.Generated Fields() {
      super();
    }
    <clinit>() {
    }
  }
  String withJavadoc;
  String withoutJavadoc;
  FieldNameConstantsJavadoc2() {
    super();
  }
}
