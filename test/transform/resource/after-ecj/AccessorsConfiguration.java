class AccessorsConfiguration {
  private @lombok.Getter @lombok.Setter @lombok.experimental.Accessors(fluent = true) String m_FieldName = "";
  AccessorsConfiguration() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String fieldName() {
    return this.m_FieldName;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void fieldName(final String m_FieldName) {
    this.m_FieldName = m_FieldName;
  }
}
@lombok.experimental.Accessors(prefix = {}) class AccessorsConfiguration2 {
  private @lombok.Setter String m_FieldName = "";
  AccessorsConfiguration2() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setM_FieldName(final String m_FieldName) {
    this.m_FieldName = m_FieldName;
  }
}
@lombok.experimental.Accessors(chain = true) class AccessorsConfiguration3 {
  private @lombok.Setter String fFieldName = "";
  AccessorsConfiguration3() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") AccessorsConfiguration3 setFieldName(final String fFieldName) {
    this.fFieldName = fFieldName;
    return this;
  }
}
