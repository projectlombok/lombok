//version 9:
//platform !eclipse: TestEclipse boots with JDK 8 java.lang.Deprecated, which has no since/forRemoval members.
import lombok.Setter;
class SetterDeprecatedMembers {
	@Deprecated(since = "1.2", forRemoval = true)
	@Setter int both;
}
