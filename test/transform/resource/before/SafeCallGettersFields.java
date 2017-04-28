package first.second;

import lombok.experimental.SafeCall;

class SafeCallGettersFields {

	public SafeCallGettersFields nullSafeCall;
	public SafeCallGettersFields nullSafeCall2;
	public String nullString;

	public static @SafeCall
	String s = new SafeCallGettersFields().getNullString().trim();
	public @SafeCall
	String s2 = getNullSafeCall().nullSafeCall.getNullString().trim();
	public @SafeCall
	String s3 = nullSafeCall.getNullSafeCall().getNullString().trim();
	public @SafeCall
	String s4;
	public @SafeCall
	String s5 = "";
	public @SafeCall
	SafeCallGettersFields s1 = nullSafeCall;
	
	public SafeCallGettersFields() {
		@SafeCall java.lang.String s = new first.second.SafeCallGettersFields().getNullString().trim();
		int s11 = 0;
		{
			String s13 = "";
			{
				@SafeCall String s1 = getNullSafeCall().getNullSafeCall2().getNullString().trim();
			}
		}
		@SafeCall SafeCallGettersFields nullSafeCall = getNullSafeCall();
		@SafeCall String s2 = nullSafeCall.nullSafeCall2.getNullString().trim();
		@SafeCall String s3 = nullSafeCall.nullSafeCall.nullString.trim();
		@SafeCall String s4 = nullSafeCall.getNullSafeCall().nullSafeCall.nullString.trim();
		@SafeCall String s5 = this.nullSafeCall.nullString.trim();
	}
	
	public String getNullString() {
		return null;
	}
	
	public SafeCallGettersFields getNullSafeCall() {
		return null;
	}
	public SafeCallGettersFields getNullSafeCall2() {
		return null;
	}
}