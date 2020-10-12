// version 9: the 'this' param option exists in java8, but is bugged, in that annotations are not allowed on them, even without a @Target. The only purpose of the this param is annotations, so, boy, isn't that a punch in the face?
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

class ThisParameter {
	
	void untagged(ThisParameter this, int i) {
		// no content
	}
	
	void sourceTagged(@SourceTagged("source") ThisParameter this) {
		// no content
	}
	
	void classTagged(@ClassTagged("class") ThisParameter this) {
		// no content
	}	
	
	void runtimeTagged(@RuntimeTagged("runtime") ThisParameter this) {
		// no content
	}
	
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.SOURCE)
	@interface SourceTagged {
		String value();
	}
	
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.CLASS)
	@interface ClassTagged {
		String value();
	}
	
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@interface RuntimeTagged {
		String value();
	}
}
