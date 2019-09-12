import lombok.Builder(setterPrefix = "with");
@Builder(toBuilder = true, builderMethodName = "", setterPrefix = "with")
class BuilderWithNoBuilderMethod {
	private String a = "";
}
