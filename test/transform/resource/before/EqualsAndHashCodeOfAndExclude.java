@lombok.EqualsAndHashCode(of={"x"})
final class EqualsAndHashCodeOf {
	int x;
	int y;
}

@lombok.EqualsAndHashCode(exclude={"y"})
final class EqualsAndHashCodeExclude {
	int x;
	int y;
}

@lombok.EqualsAndHashCode
final class EqualsAndHashCodeOfField {
	@lombok.EqualsAndHashCode.Of
	int x;
	int y;
}

@lombok.EqualsAndHashCode
final class EqualsAndHashCodeExcludeField {
	int x;
	@lombok.EqualsAndHashCode.Exclude
	int y;
}
