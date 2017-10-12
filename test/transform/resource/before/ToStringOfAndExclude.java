@lombok.ToString(of={"x"})
class ToStringOf {
    int x;
    int y;
}
@lombok.ToString(exclude={"y"})
class ToStringExclude {
    int x;
    int y;
}
@lombok.ToString
class ToStringOfField {
    @lombok.ToString.Of
    int x;
    int y;
}
@lombok.ToString
class ToStringExcludeField {
    int x;
    @lombok.ToString.Exclude
    int y;
}
