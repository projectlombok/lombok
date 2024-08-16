public class BuilderExcludesWarnings {
    long x;
    int y;
    int z;

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    BuilderExcludesWarnings(final long x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static class BuilderExcludesWarningsBuilder {
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
        BuilderExcludesWarningsBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWarnings.BuilderExcludesWarningsBuilder x(final long x) {
            this.x = x;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWarnings.BuilderExcludesWarningsBuilder y(final int y) {
            this.y = y;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public BuilderExcludesWarnings build() {
            return new BuilderExcludesWarnings(this.x, this.y, this.z);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public java.lang.String toString() {
            return "BuilderExcludesWarnings.BuilderExcludesWarningsBuilder(x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static BuilderExcludesWarnings.BuilderExcludesWarningsBuilder builder() {
        return new BuilderExcludesWarnings.BuilderExcludesWarningsBuilder();
    }
}

class NoBuilderButHasExcludes {
    long x;
    int y;
    int z;

    public NoBuilderButHasExcludes(long x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static class NoBuilderButHasExcludesBuilder {
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
        NoBuilderButHasExcludesBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder x(final long x) {
            this.x = x;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder y(final int y) {
            this.y = y;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder z(final int z) {
            this.z = z;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public NoBuilderButHasExcludes build() {
            return new NoBuilderButHasExcludes(this.x, this.y, this.z);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public java.lang.String toString() {
            return "NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder(x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public static NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder builder() {
        return new NoBuilderButHasExcludes.NoBuilderButHasExcludesBuilder();
    }
}