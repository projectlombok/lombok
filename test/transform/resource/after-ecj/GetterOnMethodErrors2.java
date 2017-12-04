class GetterOnMethodErrors2 {
  public @interface Test {
  }
  private @lombok.Getter() int bad1;
  private @lombok.Getter() int bad2;
  private @lombok.Getter() int bad3;
  private @lombok.Getter() int bad4;
  private @lombok.Getter() int good1;
  private @lombok.Getter() int good2;
  private @lombok.Getter() int good3;
  private @lombok.Getter() int good4;
  GetterOnMethodErrors2() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getBad1() {
    return this.bad1;
  }
  public @java.lang.SuppressWarnings("all") int getBad2() {
    return this.bad2;
  }
  public @java.lang.SuppressWarnings("all") int getBad3() {
    return this.bad3;
  }
  public @java.lang.SuppressWarnings("all") int getBad4() {
    return this.bad4;
  }
  public @java.lang.SuppressWarnings("all") int getGood1() {
    return this.good1;
  }
  public @java.lang.SuppressWarnings("all") int getGood2() {
    return this.good2;
  }
  public @Deprecated @java.lang.SuppressWarnings("all") int getGood3() {
    return this.good3;
  }
  public @Deprecated @Test @java.lang.SuppressWarnings("all") int getGood4() {
    return this.good4;
  }
}