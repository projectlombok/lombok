import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
public @FieldNameConstants class FieldNameConstantsBasic {
  public static final java.lang.String FIELD_BUT_PRINT_ME_PLEASE = "butPrintMePlease";
  protected static final java.lang.String FIELD_I_AM_A_DVD_PLAYER = "iAmADvdPlayer";
  @FieldNameConstants(level = AccessLevel.PROTECTED) String iAmADvdPlayer;
  int $skipMe;
  static double skipMeToo;
  String butPrintMePlease;
  <clinit>() {
  }
  public FieldNameConstantsBasic() {
    super();
  }
}
