import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Map;
@Setter @RequiredArgsConstructor class LombokNonNull {
  @NonNull Double[] noNullCheck;
  List<@NonNull Double> noNullCheckList;
  String @NonNull [] nullCheck;
  @NonNull List<String> nullCheckList;
  Map<@NonNull String, @NonNull List<@Nullable Object @NonNull []>> test;
  public void noNullCheckExpected1(@NonNull Double[] d) {
  }
  public void noNullCheckExpected2(@NonNull Double... d) {
  }
  public void nullCheckExpected1(Double @NonNull [] d) {
    if ((d == null))
        {
          throw new java.lang.NullPointerException("d is marked non-null but is null");
        }
  }
  public void nullCheckExpected2(Double @NonNull ... d) {
    if ((d == null))
        {
          throw new java.lang.NullPointerException("d is marked non-null but is null");
        }
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setNoNullCheck(final @NonNull Double[] noNullCheck) {
    this.noNullCheck = noNullCheck;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setNoNullCheckList(final List<@NonNull Double> noNullCheckList) {
    this.noNullCheckList = noNullCheckList;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setNullCheck(final String[] nullCheck) {
    if ((nullCheck == null))
        {
          throw new java.lang.NullPointerException("nullCheck is marked non-null but is null");
        }
    this.nullCheck = nullCheck;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setNullCheckList(final @NonNull List<String> nullCheckList) {
    if ((nullCheckList == null))
        {
          throw new java.lang.NullPointerException("nullCheckList is marked non-null but is null");
        }
    this.nullCheckList = nullCheckList;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setTest(final Map<@NonNull String, @NonNull List<@Nullable Object[]>> test) {
    this.test = test;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated LombokNonNull(final String[] nullCheck, final @NonNull List<String> nullCheckList) {
    super();
    if ((nullCheck == null))
        {
          throw new java.lang.NullPointerException("nullCheck is marked non-null but is null");
        }
    if ((nullCheckList == null))
        {
          throw new java.lang.NullPointerException("nullCheckList is marked non-null but is null");
        }
    this.nullCheck = nullCheck;
    this.nullCheckList = nullCheckList;
  }
}
