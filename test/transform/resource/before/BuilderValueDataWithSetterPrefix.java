import java.util.List;

@lombok.Builder(setterPrefix = "with") @lombok.Value
class BuilderAndValueWithSetterPrefix {
	private final int zero = 0;
}

@lombok.Builder(setterPrefix = "with") @lombok.Data
class BuilderAndDataWithSetterPrefix {
	private final int zero = 0;
}
