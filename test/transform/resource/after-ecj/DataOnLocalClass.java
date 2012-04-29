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
        if ((! (o instanceof Local)))
            return false;
        final @java.lang.SuppressWarnings("all") Local other = (Local) o;
        if ((! other.canEqual((java.lang.Object) this)))
            return false;
        if ((this.getX() != other.getX()))
            return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (((this$name == null) ? (other$name != null) : (! this$name.equals(other$name))))
            return false;
        return true;
      }
      public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
        return (other instanceof Local);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = ((result * PRIME) + this.getX());
        final java.lang.Object $name = this.getName();
        result = ((result * PRIME) + (($name == null) ? 0 : $name.hashCode()));
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
          if ((! (o instanceof Local.InnerLocal)))
              return false;
          final @java.lang.SuppressWarnings("all") InnerLocal other = (InnerLocal) o;
          if ((! other.canEqual((java.lang.Object) this)))
              return false;
          final java.lang.Object this$name = this.getName();
          final java.lang.Object other$name = other.getName();
          if (((this$name == null) ? (other$name != null) : (! this$name.equals(other$name))))
              return false;
          return true;
        }
        public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
          return (other instanceof Local.InnerLocal);
        }
        public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
          final int PRIME = 31;
          int result = 1;
          final java.lang.Object $name = this.getName();
          result = ((result * PRIME) + (($name == null) ? 0 : $name.hashCode()));
          return result;
        }
        public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
          return (("Local.InnerLocal(name=" + this.getName()) + ")");
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
        if ((! (o instanceof Local)))
            return false;
        final @java.lang.SuppressWarnings("all") Local other = (Local) o;
        if ((! other.canEqual((java.lang.Object) this)))
            return false;
        if ((this.getX() != other.getX()))
            return false;
        return true;
      }
      public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
        return (other instanceof Local);
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