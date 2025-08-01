// version 14:
// skip compare content
import lombok.NonNull;
public record NonNullOnRecordExistingConstructor(String a) {
  public NonNullOnRecordExistingConstructor {
    super();
    if ((a == null))
        {
          throw new java.lang.NullPointerException("a is marked non-null but is null");
        }
    System.out.println("Hello");
  }
}
