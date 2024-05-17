public final class BuilderAccessWithGetter {
    private final String string;
    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    BuilderAccessWithGetter(final String string) {
        this.string = string;
    }
    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    private static class BuilderAccessWithGetterBuilder {
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private String string;
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        BuilderAccessWithGetterBuilder() {
        }
        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private BuilderAccessWithGetter.BuilderAccessWithGetterBuilder string(final String string) {
            this.string = string;
            return this;
        }
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        private BuilderAccessWithGetter build() {
            return new BuilderAccessWithGetter(this.string);
        }
        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @lombok.Generated
        public java.lang.String toString() {
            return "BuilderAccessWithGetter.BuilderAccessWithGetterBuilder(string=" + this.string + ")";
        }
    }
    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    private static BuilderAccessWithGetter.BuilderAccessWithGetterBuilder builder() {
        return new BuilderAccessWithGetter.BuilderAccessWithGetterBuilder();
    }
    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public String getString() {
        return this.string;
    }
}
