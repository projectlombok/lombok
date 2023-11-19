import lombok.Setter;
public @Setter class SetterInternOnField {
  private @Setter(internString = true) String str1;
  private @Setter(internString = true) java.lang.String str2;
  private String str3;
  public SetterInternOnField() {
    super();
  }
  public @java.lang.SuppressWarnings("all") void setStr1(final String str1) {
    this.str1 = ((str1 == null) ? null : str1.intern());
  }
  public @java.lang.SuppressWarnings("all") void setStr2(final java.lang.String str2) {
    this.str2 = ((str2 == null) ? null : str2.intern());
  }
  public @java.lang.SuppressWarnings("all") void setStr3(final String str3) {
    this.str3 = str3;
  }
}