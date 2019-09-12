import lombok.Builder(setterPrefix = "with");
@Builder(toBuilder = true, builderMethodName = "", setterPrefix = "with")
class BuilderWithNoBuilderMethodWithSetterPrefix {
	private String a = "";
}
