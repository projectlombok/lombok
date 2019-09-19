import lombok.Builder
@Builder(toBuilder = true, builderMethodName = "",setterPrefix = "with")
class BuilderWithNoBuilderMethodWithSetterPrefix {
	private String a = "";
}
