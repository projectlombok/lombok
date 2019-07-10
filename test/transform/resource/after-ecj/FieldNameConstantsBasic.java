import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
public @FieldNameConstants(level = AccessLevel.PACKAGE) class FieldNameConstantsBasic {
  static final @java.lang.SuppressWarnings("all") class Fields {
    public static final java.lang.String iAmADvdPlayer = "iAmADvdPlayer";
    public static final java.lang.String butPrintMePlease = "butPrintMePlease";
    <clinit>() {
    }
    private @java.lang.SuppressWarnings("all") Fields() {
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
  public FieldNameConstantsBasic() {
    super();
  }
}