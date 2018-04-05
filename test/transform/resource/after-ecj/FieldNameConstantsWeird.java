import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
public @FieldNameConstants class FieldNameConstantsWeird {
  @FieldNameConstants(level = AccessLevel.NONE) String iAmADvdPlayer;
  String X;
  public FieldNameConstantsWeird() {
    super();
  }
}
