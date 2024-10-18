public class BuilderExcludesWithAllArgsConstructor {
    long x;
    int y;
    int z;


    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static class BuilderExcludesWithAllArgsConstructorBuilder {
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private long x;
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private int y;
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private int z;

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        BuilderExcludesWithAllArgsConstructorBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder x(final long x) {
            this.x = x;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder y(final int y) {
            this.y = y;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWithAllArgsConstructor build() {
            return new BuilderExcludesWithAllArgsConstructor(this.x, this.y, this.z);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public java.lang.String toString() {
            return "BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder(x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder builder() {
        return new BuilderExcludesWithAllArgsConstructor.BuilderExcludesWithAllArgsConstructorBuilder();
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public BuilderExcludesWithAllArgsConstructor(final long x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
  