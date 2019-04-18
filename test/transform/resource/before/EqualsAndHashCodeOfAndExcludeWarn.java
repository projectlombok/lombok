// skip-compare-contents
@lombok.EqualsAndHashCode(of={"y"})
final class EqualsAndHashCodeWarnOf {
	int x;
}

@lombok.EqualsAndHashCode(exclude={"y"})
final class EqualsAndHashCodeWarnExclude {
	int x;
}
