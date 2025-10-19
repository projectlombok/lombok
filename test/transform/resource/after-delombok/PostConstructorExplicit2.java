// version 8:
class PostConstructorExplicit2 {
	private void first() {
	}

	private void second() {
	}

	public PostConstructorExplicit2(long tryFinally) throws java.io.IOException {
		boolean $callPostConstructor = true;
		try {
			if ("a".equals("b")) {
				return;
			}
			if ("a".equals("b")) {
				throw new java.io.IOException("");
			}
		} catch (final java.lang.Throwable $ex) {
			$callPostConstructor = false;
			throw $ex;
		} finally {
			if ($callPostConstructor) {
				first();
				second();
			}
		}
	}

	public PostConstructorExplicit2(int innerExit) {
		Runnable r1 = new Runnable() {
			public void run() {
				return;
			}
		};
		first();
		second();
	}

	public PostConstructorExplicit2(short lambdaExit) {
		Runnable r2 = () -> {
			return;
		};
		first();
		second();
	}
}
