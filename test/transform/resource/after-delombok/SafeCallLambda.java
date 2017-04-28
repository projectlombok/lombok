// version 8:
class SafeCallLambda {
	public SafeCallLambda() {
		Runnable r = () -> {
			int i;
			{
				java.lang.Integer i1 = getNullInteger();
				i = i1 != null ? i1 : 0;
			}
		};
	}

	public Integer getNullInteger() {
		return null;
	}
}