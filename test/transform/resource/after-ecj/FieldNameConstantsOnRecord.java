// version 14:
import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
public @FieldNameConstants(level = AccessLevel.PACKAGE) record FieldNameConstantsOnRecord(String iAmADvdPlayer, int $skipMe, int andMe, String butPrintMePlease) {
  static final @java.lang.SuppressWarnings("all") @lombok.Generated class Fields {
    public static final java.lang.String iAmADvdPlayer = "iAmADvdPlayer";
    public static final java.lang.String butPrintMePlease = "butPrintMePlease";
    <clinit>() {
    }
    private @java.lang.SuppressWarnings("all") @lombok.Generated Fields() {
      super();
    }
  }
  static double skipMeToo;
  <clinit>() {
  }
}
