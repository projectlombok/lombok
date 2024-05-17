import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
@FieldNameConstants(asEnum = true,innerTypeName = "TypeTest") class FieldNameConstantsHandrolled1 {
  public enum TypeTest {
    field1(),
    alsoAField(),
    thirdField(),
    <clinit>() {
    }
    private @java.lang.SuppressWarnings("all") @lombok.Generated TypeTest() {
      super();
    }
  }
  int field1;
  int alsoAField;
  int thirdField;
  FieldNameConstantsHandrolled1() {
    super();
  }
}
@FieldNameConstants(asEnum = true,innerTypeName = "TypeTest") class FieldNameConstantsHandrolled2 {
  public enum TypeTest {
    field1(),
    alsoAField(),
    thirdField(),
    <clinit>() {
    }
    public String foo() {
      return name();
    }
    private @java.lang.SuppressWarnings("all") @lombok.Generated TypeTest() {
      super();
    }
  }
  int field1;
  int alsoAField;
  int thirdField;
  FieldNameConstantsHandrolled2() {
    super();
  }
}
@FieldNameConstants class FieldNameConstantsHandrolled3 {
  static class Fields {
    public static final java.lang.String field1 = "field1";
    public static final java.lang.String thirdField = "thirdField";
    public static final int alsoAField = 5;
    <clinit>() {
    }
    private @java.lang.SuppressWarnings("all") @lombok.Generated Fields() {
      super();
    }
  }
  int field1;
  int alsoAField;
  int thirdField;
  FieldNameConstantsHandrolled3() {
    super();
  }
}
