public class BuilderExcludesWithDefault {
    long x;
    int y;
    int z;

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    private static int $default$z() {
        return 5;
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    BuilderExcludesWithDefault(final long x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static class BuilderExcludesWithDefaultBuilder {
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private long x;
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private int y;
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private boolean z$set;
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private int z$value;

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        BuilderExcludesWithDefaultBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder x(final long x) {
            this.x = x;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder y(final int y) {
            this.y = y;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWithDefault build() {
            int z$value = this.z$value;
            if (!this.z$set) z$value = BuilderExcludesWithDefault.$default$z();
            return new BuilderExcludesWithDefault(this.x, this.y, z$value);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public java.lang.String toString() {
            return "BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder(x=" + this.x + ", y=" + this.y + ", z$value=" + this.z$value + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder builder() {
        return new BuilderExcludesWithDefault.BuilderExcludesWithDefaultBuilder();
    }
}