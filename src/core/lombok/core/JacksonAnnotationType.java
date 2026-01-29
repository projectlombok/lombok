package lombok.core;

public enum JacksonAnnotationType {
	JSON_POJO_BUILDER2("com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder"),
	JSON_POJO_BUILDER3("tools.jackson.databind.annotation.JsonPOJOBuilder"),
	JSON_DESERIALIZE2("com.fasterxml.jackson.databind.annotation.JsonDeserialize"),
	JSON_DESERIALIZE3("tools.jackson.databind.annotation.JsonDeserialize"),
	JSON_PROPERTY2("com.fasterxml.jackson.annotation.JsonProperty"),
	JSON_IGNORE2("com.fasterxml.jackson.annotation.JsonIgnore"),
	;
	
	private final String qualifiedName;
	private final String[] qualifiedNameStringArr;
	private final char[][] qualifiedNameCharArrArr;
	
	private JacksonAnnotationType(final String qualifiedName) {
		this.qualifiedName = qualifiedName;
		String[] parts = qualifiedName.split("\\.");
		this.qualifiedNameStringArr = parts;
		char[][] caa = new char[parts.length][];
		for (int i = 0; i < parts.length; i++) {
			caa[i] = parts[i].toCharArray();
		}
		this.qualifiedNameCharArrArr = caa;
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}
	
	// Do not modify the returned array.
	public String[] getQualifiedNameAsStringArray() {
		return qualifiedNameStringArr;
	}
	
	// Do not modify the returned array.
	public char[][] getQualifiednameAsCharArrayArray() {
		return qualifiedNameCharArrArr;
	}
}
