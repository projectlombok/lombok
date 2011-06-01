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
        if ((java.util.Collections.singletonList(o).get(0) != null))
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
        if ((java.util.Collections.singletonList(o).get(0) != null))
            {
              o.toString();
            }
      }
  }
}