import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;

@FieldNameConstants
public class FieldNameConstantsWeird {
	@FieldNameConstants(level = AccessLevel.NONE)
	String iAmADvdPlayer;
	String X;
}
