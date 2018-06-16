import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
public @FieldNameConstants class FieldNameConstantsWeird {
  public static final java.lang.String FIELD_AZ = "A";
  @FieldNameConstants(level = AccessLevel.NONE) String iAmADvdPlayer;
  @FieldNameConstants(prefix = "") String X;
  @FieldNameConstants(suffix = "Z") String A;
  <clinit>() {
  }
  public FieldNameConstantsWeird() {
    super();
  }
}
