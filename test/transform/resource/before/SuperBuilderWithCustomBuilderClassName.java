//CONF: lombok.builder.className=Builder
class SuperBuilderWithCustomBuilderClassName {
	@lombok.SuperBuilder
	static class SuperClass {
	}
	@lombok.SuperBuilder
	static class SubClass extends SuperClass {
	}
}
