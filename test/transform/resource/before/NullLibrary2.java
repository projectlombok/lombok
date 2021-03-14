//version 8: springframework dep is too new to run on j6
//CONF: lombok.addNullAnnotations = spring
@lombok.EqualsAndHashCode
@lombok.ToString
@lombok.AllArgsConstructor
public class NullLibrary2 {
	@lombok.With String foo;
}
