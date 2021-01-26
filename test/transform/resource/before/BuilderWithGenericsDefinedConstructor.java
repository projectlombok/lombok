import java.util.List;
@lombok.Builder
class BuilderWithGenericsDefinedConstructor<T extends Number, V extends T> {
	private V a;
	
	public BuilderWithGenericsDefinedConstructor(T b) {}
}
