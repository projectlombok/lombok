// skip-compare-contents
@lombok.EqualsAndHashCode(of={"x", Const.A})
final class EqualsAndHashCodeErrorOf {
	int x;
}

@lombok.EqualsAndHashCode(exclude={"x", Const.A})
final class EqualsAndHashCodeErrorExclude {
	int x;
}

class Const {
	static final String A = "A";
}