// version 14:
// skip compare content: At time of writing, the `@NonNull` annotation causes our handler to produce an explicit-params constructor (i.e. `public NNOREC(String a) {` instead of `public NNOREC {`) with `@NonNull`, and in the statements the null check. But (?) other eclipse versions might not, hence, we ignore content.
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
