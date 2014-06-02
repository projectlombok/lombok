//CONF: lombok.Accessors.prefix += m_
//CONF: lombok.Accessors.prefix += f
//CONF: lombok.Accessors.chain = false

class AccessorsConfiguration {
	@lombok.Getter @lombok.Setter @lombok.experimental.Accessors(fluent=true)
	private String m_FieldName = "";
}

@lombok.experimental.Accessors(prefix = {})
class AccessorsConfiguration2 {
	@lombok.Setter
	private String m_FieldName = "";
}

@lombok.experimental.Accessors(chain = true)
class AccessorsConfiguration3 {
	@lombok.Setter
	private String fFieldName = "";
}
