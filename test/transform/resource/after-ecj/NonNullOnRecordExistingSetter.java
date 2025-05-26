// version 19:
import lombok.NonNull;
public record NonNullOnRecordExistingSetter(String a) {
  public NonNullOnRecordExistingSetter(String a) {
    super();
    this.a = a;
  }
  public void method(@NonNull String param) {
    if ((param == null))
        {
          throw new java.lang.NullPointerException("param is marked non-null but is null");
        }
    String asd = "a";
  }
}
