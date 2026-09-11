import lombok.Builder;

@Builder(toBuilder = true, toBuilderMethodName = "mutate")
class BuilderToBuilderMethodName {
	private String name;
	private int age;
}
