import java.util.Set;
@lombok.experimental.SuperBuilder
class SuperBuilderSingularCustomized {
	@lombok.Singular private Set<String> foos;
    public static abstract class SuperBuilderSingularCustomizedBuilder<C extends SuperBuilderSingularCustomized, B extends SuperBuilderSingularCustomized.SuperBuilderSingularCustomizedBuilder<C, B>> {
        public B custom(final String value) {
            return self(); 
        }
    }}
