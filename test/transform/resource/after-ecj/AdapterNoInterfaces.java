import lombok.experimental.Adapter;
public class AdapterNoInterfaces {
  public static @Adapter class AdapterClass {
    public AdapterClass() {
      super();
    }
    public String getStatus() {
      return "TestStatus";
    }
  }
  public AdapterNoInterfaces() {
    super();
  }
}
