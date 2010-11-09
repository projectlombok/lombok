class CleanupName {
  CleanupName() {
    super();
  }
  void test() {
    @lombok.Cleanup("toString") Object o = "Hello World!";
    try 
      {
        System.out.println(o);
      }
    finally
      {
        if ((o != null))
            {
              o.toString();
            }
      }
  }
  void test2() {
    @lombok.Cleanup(value = "toString") Object o = "Hello World too!";
    try 
      {
        System.out.println(o);
      }
    finally
      {
        if ((o != null))
            {
              o.toString();
            }
      }
  }
}