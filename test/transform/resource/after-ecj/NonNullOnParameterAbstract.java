abstract class NonNullOnParameterAbstract {
  NonNullOnParameterAbstract() {
    super();
  }
  public void test(@lombok.NonNull String arg) {
    if ((arg == null))
        {
          throw new java.lang.NullPointerException("arg is marked @NonNull but is null");
        }
    System.out.println("Hey");
  }
  public abstract void test2(@lombok.NonNull String arg);
}