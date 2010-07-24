import lombok.Data;
class DataOnLocalClass1 {
  DataOnLocalClass1() {
    super();
  }
  public static void main(String[] args) {
    @Data class Local {
      final int x;
      String name;
      public @java.lang.SuppressWarnings("all") Local(final int x) {
        super();
        this.x = x;
      }
      public @java.lang.SuppressWarnings("all") int getX() {
        return this.x;
      }
      public @java.lang.SuppressWarnings("all") String getName() {
        return this.name;
      }
      public @java.lang.SuppressWarnings("all") void setName(final String name) {
        this.name = name;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
        if ((o == this))
            return true;
        if ((o == null))
            return false;
        if ((o.getClass() != this.getClass()))
            return false;
        final Local other = (Local) o;
        if ((this.getX() != other.getX()))
            return false;
        if (((this.getName() == null) ? (other.getName() != null) : (! this.getName().equals(other.getName()))))
            return false;
        return true;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = ((result * PRIME) + this.getX());
        result = ((result * PRIME) + ((this.getName() == null) ? 0 : this.getName().hashCode()));
        return result;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("Local(x=" + this.getX()) + ", name=") + this.getName()) + ")");
      }
    }
  }
}
class DataOnLocalClass2 {
  {
    @Data class Local {
      @Data class InnerLocal {
        @lombok.NonNull String name;
        public @java.lang.SuppressWarnings("all") InnerLocal(final @lombok.NonNull String name) {
          super();
          if ((name == null))
              throw new java.lang.NullPointerException("name");
          this.name = name;
        }
        public @lombok.NonNull @java.lang.SuppressWarnings("all") String getName() {
          return this.name;
        }
        public @java.lang.SuppressWarnings("all") void setName(final @lombok.NonNull String name) {
          if ((name == null))
              throw new java.lang.NullPointerException("name");
          this.name = name;
        }
        public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
          if ((o == this))
              return true;
          if ((o == null))
              return false;
          if ((o.getClass() != this.getClass()))
              return false;
          final InnerLocal other = (InnerLocal) o;
          if (((this.getName() == null) ? (other.getName() != null) : (! this.getName().equals(other.getName()))))
              return false;
          return true;
        }
        public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
          final int PRIME = 31;
          int result = 1;
          result = ((result * PRIME) + ((this.getName() == null) ? 0 : this.getName().hashCode()));
          return result;
        }
        public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
          return (("InnerLocal(name=" + this.getName()) + ")");
        }
      }
      final int x;
      public @java.lang.SuppressWarnings("all") Local(final int x) {
        super();
        this.x = x;
      }
      public @java.lang.SuppressWarnings("all") int getX() {
        return this.x;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
        if ((o == this))
            return true;
        if ((o == null))
            return false;
        if ((o.getClass() != this.getClass()))
            return false;
        final Local other = (Local) o;
        if ((this.getX() != other.getX()))
            return false;
        return true;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = ((result * PRIME) + this.getX());
        return result;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("Local(x=" + this.getX()) + ")");
      }
    }
  }
  DataOnLocalClass2() {
    super();
  }
}