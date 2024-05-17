class BuilderNestedInEnum {
  public enum TestEnum {
    public static final @lombok.Builder @lombok.Value class TestBuilder {
      public static @java.lang.SuppressWarnings("all") @lombok.Generated class TestBuilderBuilder {
        private @java.lang.SuppressWarnings("all") @lombok.Generated String field;
        @java.lang.SuppressWarnings("all") @lombok.Generated TestBuilderBuilder() {
          super();
        }
        /**
         * @return {@code this}.
         */
        public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder field(final String field) {
          this.field = field;
          return this;
        }
        public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedInEnum.TestEnum.TestBuilder build() {
          return new BuilderNestedInEnum.TestEnum.TestBuilder(this.field);
        }
        public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
          return (("BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder(field=" + this.field) + ")");
        }
      }
      private final String field;
      @java.lang.SuppressWarnings("all") @lombok.Generated TestBuilder(final String field) {
        super();
        this.field = field;
      }
      public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder builder() {
        return new BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated String getField() {
        return this.field;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
        if ((o == this))
            return true;
        if ((! (o instanceof BuilderNestedInEnum.TestEnum.TestBuilder)))
            return false;
        final BuilderNestedInEnum.TestEnum.TestBuilder other = (BuilderNestedInEnum.TestEnum.TestBuilder) o;
        final java.lang.Object this$field = this.getField();
        final java.lang.Object other$field = other.getField();
        if (((this$field == null) ? (other$field != null) : (! this$field.equals(other$field))))
            return false;
        return true;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $field = this.getField();
        result = ((result * PRIME) + (($field == null) ? 43 : $field.hashCode()));
        return result;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("BuilderNestedInEnum.TestEnum.TestBuilder(field=" + this.getField()) + ")");
      }
    }
    FOO(),
    BAR(),
    <clinit>() {
    }
    public TestEnum() {
      super();
    }
  }
  BuilderNestedInEnum() {
    super();
  }
}
