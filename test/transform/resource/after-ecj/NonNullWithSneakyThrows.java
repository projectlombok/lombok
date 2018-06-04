class NonNullWithSneakyThrows {
  NonNullWithSneakyThrows() {
    super();
  }
  @lombok.SneakyThrows void test(@lombok.NonNull String in) {
    try 
      {
        if ((in == null))
            {
              throw new java.lang.NullPointerException("in is marked @NonNull but is null");
            }
        System.out.println(in);
      }
    catch (final java.lang.Throwable $ex)
      {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
}