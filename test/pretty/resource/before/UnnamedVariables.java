// version 22:
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

record Box<T>(T content) {
}

record Pair<T, U>(T first, U second) {
}

public class UnnamedVariables {
	void enhancedLoop() {
		for (String _ : Arrays.asList("")) {
		}
	}

	void initializationInForLoop() {
		for (int i = 0, _ = Integer.MAX_VALUE; i < 10; i++) {
		}
	}

	void assignment() {
		var _ = 1;
	}

	void catchBlock() {
		try {
		} catch (Exception _) {
		}
	}

	void tryWithResources() {
		try (var _ = new Scanner("")) {
		}
	}

	void lambda() {
		Stream.of(1).forEach(_ -> {
		});
	}

	Object switchStatement(Object o) {
		return switch (o) {
			case String _, Integer _ -> o;
			case Long _ -> o;
			default -> o;
		};
	}

	void recordPattern(Object o) {
		if (o instanceof Pair(Box _, Box(Integer _))) {
		}
		if (o instanceof Box(String _)) {
		}
		if (o instanceof Box(var _)) {
		}
		if (o instanceof Box(_)) {
		}
	}

	Object recordPatternSwitch(Object o) {
		return switch (o) {
			case Box(String _), Box(Integer _) -> o;
			case Box(Long _) -> o;
			case Box(_) -> o;
			default -> o;
		};
	}
}
