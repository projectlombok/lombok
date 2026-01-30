package lombok.core.configuration;

public enum JacksonVersion implements MappedConfigEnum {
	TWO,
	THREE,
	;
	
	@Override public boolean matches(String value) {
		if (this == TWO) return "2".equals(value);
		return "3".equals(value);
	}
	
	@Override public String toString() {
		if (this == TWO) return "2";
		return "3";
	}
}
