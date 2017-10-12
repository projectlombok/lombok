class ToStringOf {
    int x;
    int y;
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public java.lang.String toString() {
        return "ToStringOf(x=" + this.x + ")";
    }
}
class ToStringExclude {
    int x;
    int y;
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public java.lang.String toString() {
        return "ToStringExclude(x=" + this.x + ")";
    }
}
class ToStringOfField {
    @lombok.ToString.Of
    int x;
    int y;
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public java.lang.String toString() {
        return "ToStringOfField(x=" + this.x + ")";
    }
}
class ToStringExcludeField {
    int x;
    @lombok.ToString.Exclude
    int y;
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public java.lang.String toString() {
        return "ToStringExcludeField(x=" + this.x + ")";
    }
}
