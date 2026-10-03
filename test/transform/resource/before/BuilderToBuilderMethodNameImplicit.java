import lombok.Builder;

@Builder(toBuilderMethodName = "mutate")
class BuilderToBuilderMethodNameImplicit {
	private String name;
	private int age;
}
