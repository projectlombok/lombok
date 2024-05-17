import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
public @FieldNameConstants(level = AccessLevel.PACKAGE) class FieldNameConstantsUppercased {
  static final @java.lang.SuppressWarnings("all") @lombok.Generated class Fields {
    public static final java.lang.String I_AM_A_DVD_PLAYER = "iAmADvdPlayer";
    public static final java.lang.String BUT_PRINT_ME_PLEASE = "butPrintMePlease";
    <clinit>() {
    }
    private @java.lang.SuppressWarnings("all") @lombok.Generated Fields() {
      super();
    }
  }
  String iAmADvdPlayer;
  int $skipMe;
  static double skipMeToo;
  @FieldNameConstants.Exclude int andMe;
  String butPrintMePlease;
  <clinit>() {
  }
  public FieldNameConstantsUppercased() {
    super();
  }
}
