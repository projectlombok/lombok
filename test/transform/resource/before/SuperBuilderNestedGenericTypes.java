public class SuperBuilderNestedGenericTypes {
	@lombok.experimental.SuperBuilder
	public static abstract class Generic<T extends Generic<?>> {
	}
	
	@lombok.experimental.SuperBuilder
	public static abstract class NestedGeneric<T extends OtherGeneric<?>> extends Generic<NestedGeneric<? extends OtherGeneric<?>>> {
	}
	
	public interface OtherGeneric<T> {
	}
}
