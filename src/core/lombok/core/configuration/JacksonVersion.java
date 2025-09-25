package lombok.core.configuration;

public final class JacksonVersion implements ConfigurationValueType {

	private final boolean useJackson2;
	private final boolean useJackson3;

	public static final JacksonVersion _2 = new JacksonVersion(true,  false);
	public static final JacksonVersion _3 = new JacksonVersion(false,  true);
	public static final JacksonVersion _23 = new JacksonVersion(true,  true);

	public JacksonVersion(boolean useJackson2, boolean useJackson3) {
		this.useJackson2 = useJackson2;
		this.useJackson3 = useJackson3;
	}

	public static String exampleValue() {
		return "2";
	}

	public static String description() {
		return "Select the major version of the jackson framework.";
	}

	public static JacksonVersion valueOf(String value) {
		if (value == null || value.isEmpty()) {
			return _2;
		} else if ("2".equals(value.trim())) {
			return _2;
		} else if ("3".equals(value.trim())) {
			return _3;
		} else if ("2_3".equals(value.trim())) {
			return _23;
		} else{
			throw new IllegalArgumentException("Unsupported Jackson version selector. Supported values: 2, 3 or 2_3 ");
		}
	}

	public boolean useJackson2() {
		return useJackson2;
	}

	public boolean useJackson3() {
		return useJackson3;
	}

	public boolean isValid() {
		return useJackson2 || useJackson3;
	}
}
