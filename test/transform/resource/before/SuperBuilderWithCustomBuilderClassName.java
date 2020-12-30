//CONF: lombok.builder.className=Builder
class SuperBuilderWithCustomBuilderClassName {
	@lombok.experimental.SuperBuilder
	static class SuperClass {
	}
	@lombok.experimental.SuperBuilder
	static class SubClass extends SuperClass {
	}
}
