// version 14:

import lombok.experimental.FieldNameConstants;
import lombok.AccessLevel;

@FieldNameConstants(level = AccessLevel.PACKAGE)
record FieldNameConstantsOnRecord(String iAmADvdPlayer, int $skipMe, @FieldNameConstants.Exclude int andMe, String butPrintMePlease) {
	static double skipMeToo;
}