import lombok.Data;
class DataWithOverrideEqualsAndHashCode {
  class Data1 {
    Data1() {
      super();
    }
  }
  @Data class Data2 extends Data1 {
    public int hashCode() {
      return 42;
    }
    public boolean equals(Object other) {
      return false;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return "DataWithOverrideEqualsAndHashCode.Data2()";
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated Data2() {
      super();
    }
  }
  DataWithOverrideEqualsAndHashCode() {
    super();
  }
}
