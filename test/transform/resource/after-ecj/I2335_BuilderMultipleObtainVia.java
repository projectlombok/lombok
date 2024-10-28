import lombok.Builder;
public @Builder class I2335_BuilderMultipleObtainVia {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class I2335_BuilderMultipleObtainViaBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String theString;
    private @java.lang.SuppressWarnings("all") @lombok.Generated Long theLong;
    @java.lang.SuppressWarnings("all") @lombok.Generated I2335_BuilderMultipleObtainViaBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder theString(final String theString) {
      this.theString = theString;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder theLong(final Long theLong) {
      this.theLong = theLong;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated I2335_BuilderMultipleObtainVia build() {
      return new I2335_BuilderMultipleObtainVia(this.theString, this.theLong);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder(theString=" + this.theString) + ", theLong=") + this.theLong) + ")");
    }
  }
  private String theString;
  private Long theLong;
  public @Builder(toBuilder = true) I2335_BuilderMultipleObtainVia(@Builder.ObtainVia(method = "getTheString") String theString, @Builder.ObtainVia(method = "getTheLong") Long theLong) {
    super();
    setTheString(theString);
    setTheLong(theLong);
  }
  public String getTheString() {
    return theString;
  }
  public Long getTheLong() {
    return theLong;
  }
  public void setTheString(String theString) {
    this.theString = theString;
  }
  public void setTheLong(Long theLong) {
    this.theLong = theLong;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder builder() {
    return new I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder toBuilder() {
    final String theString = this.getTheString();
    final Long theLong = this.getTheLong();
    return new I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder().theString(theString).theLong(theLong);
  }
}
