import lombok.Synchronized;
public record SynchronizedInRecord(String a, String b) {
  public @Synchronized void foo() {
    String foo = "bar";
  }
}
