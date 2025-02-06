public class BuilderExcludes {
    long x;
    int y;
    int z;

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    BuilderExcludes(final long x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static class BuilderExcludesBuilder {
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
        BuilderExcludesBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludes.BuilderExcludesBuilder x(final long x) {
            this.x = x;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludes.BuilderExcludesBuilder y(final int y) {
            this.y = y;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludes build() {
            return new BuilderExcludes(this.x, this.y, this.z);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public java.lang.String toString() {
            return "BuilderExcludes.BuilderExcludesBuilder(x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static BuilderExcludes.BuilderExcludesBuilder builder() {
        return new BuilderExcludes.BuilderExcludesBuilder();
    }
}