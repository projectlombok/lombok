import lombok.experimental.SafeCall;

import java.util.Arrays;

class SafeCallCheckDuplicateVariable {
	Runnable r;
	Thread t;

	public SafeCallCheckDuplicateVariable() {
		String s3 = "";
		{
			String s1 = "";
			{
				@SafeCall String s = new SafeCallCheckDuplicateVariable().getNullSafeCall().getNullString().trim();
				String s2 = "";
			}
		}
		String s4 = "";
		String s5 = "";

		int i1 = 0;
		for (int i2 = 0; i2 < 5; ++i2) {
			@SafeCall int i = getNullString().length();
		}

		for (int i2 : Arrays.asList(1, 2)) {
			@SafeCall int i = getNullString().length();
		}

		r = new Runnable() {
			@Override
			public void run() {
				@SafeCall int i = getNullString().length();
			}
		};

		t = new Thread() {
			@Override
			public void run() {
				@SafeCall int i = getNullString().length();
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