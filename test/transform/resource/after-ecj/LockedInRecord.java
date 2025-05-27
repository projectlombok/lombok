import lombok.Locked;
public record LockedInRecord(String a, String b) {
  public @Locked void foo() {
    String foo = "bar";
  }
}
