//version 9:
//platform !eclipse: TestEclipse boots with JDK 8 java.lang.Deprecated, which has no since/forRemoval members.
import lombok.Getter;
class GetterDeprecatedMembers {
	@Deprecated(since = "1.2", forRemoval = true)
	@Getter int both;
	
	@Deprecated(since = "3")
	@Getter int sinceOnly;
	
	@Deprecated(forRemoval = true)
	@Getter int forRemovalOnly;
}
