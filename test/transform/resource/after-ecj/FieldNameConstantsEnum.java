import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;
public @FieldNameConstants(onlyExplicitlyIncluded = true,asEnum = true,innerTypeName = "TypeTest") class FieldNameConstantsEnum {
  public @java.lang.SuppressWarnings("all") @lombok.Generated enum TypeTest {
    iAmADvdPlayer(),
    $dontSkipMe(),
    alsoDontSkipMe(),
    private @java.lang.SuppressWarnings("all") @lombok.Generated TypeTest() {
      super();
    }
    <clinit>() {
    }
  }
  @FieldNameConstants.Include String iAmADvdPlayer;
  @FieldNameConstants.Include int $dontSkipMe;
  static @FieldNameConstants.Include double alsoDontSkipMe;
  int butSkipMe;
  <clinit>() {
  }
  public FieldNameConstantsEnum() {
    super();
  }
}
