
import java.util.Arrays;

class SafeCallCheckDuplicateVariable {
	Runnable r;
	Thread t;

	public SafeCallCheckDuplicateVariable() {
		String s3 = "";
		{
			String s1 = "";
			{
				String s;
				{
					SafeCallCheckDuplicateVariable s6 = new SafeCallCheckDuplicateVariable();
					SafeCallCheckDuplicateVariable s5 = s6 != null ? s6.getNullSafeCall() : null;
					java.lang.String s4 = s5 != null ? s5.getNullString() : null;
					java.lang.String s2 = s4 != null ? s4.trim() : null;
					s = s2;
				}
				String s2 = "";
			}
		}
		String s4 = "";
		String s5 = "";
		int i1 = 0;
		for (int i2 = 0; i2 < 5; ++i2) {
			int i;
			{
				java.lang.String i4 = getNullString();
				int i3 = i4 != null ? i4.length() : 0;
				i = i3;
			}
		}
		for (int i2 : Arrays.asList(1, 2)) {
			int i;
			{
				java.lang.String i4 = getNullString();
				int i3 = i4 != null ? i4.length() : 0;
				i = i3;
			}
		}
		r = new Runnable() {
			@Override
			public void run() {
				int i;
				{
					java.lang.String i2 = getNullString();
					int i1 = i2 != null ? i2.length() : 0;
					i = i1;
				}
			}
		};
		t = new Thread() {
			@Override
			public void run() {
				int i;
				{
					java.lang.String i2 = getNullString();
					int i1 = i2 != null ? i2.length() : 0;
					i = i1;
				}
			}
		};
	}

	public String getNullString() {
		return null;
	}

	public SafeCallCheckDuplicateVariable getNullSafeCall() {
		return null;
	}
}