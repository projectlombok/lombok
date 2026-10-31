//version 9:
//platform javac: TestEclipse/ecj boot with JDK 8 java.lang.Deprecated, which has no since/forRemoval members.
import lombok.Builder;
import lombok.Singular;

@Builder
public class BuilderWithDeprecatedMembers {
	@Deprecated(since = "1.2", forRemoval = true) int both;
	@Singular @Deprecated(forRemoval = true) java.util.List<String> strings;
}
