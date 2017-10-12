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
