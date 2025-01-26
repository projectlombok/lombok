@lombok.experimental.Accessors(chain = true) class AccessorsOuter {
  class AccessorsInner1 {
    private @lombok.experimental.Accessors(prefix = "z") @lombok.Setter String zTest3;
    AccessorsInner1() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated AccessorsOuter.AccessorsInner1 setTest3(final String zTest3) {
      this.zTest3 = zTest3;
      return this;
    }
  }
  @lombok.experimental.Accessors(chain = false) class AccessorsInner2 {
    private @lombok.Setter String fTest4;
    AccessorsInner2() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated void setTest4(final String fTest4) {
      this.fTest4 = fTest4;
    }
  }
  private @lombok.Setter String fTest;
  private @lombok.experimental.Accessors(prefix = "z") @lombok.Setter String zTest2;
  AccessorsOuter() {
    super();
  }
  /**
   * @return {@code this}.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated AccessorsOuter setTest(final String fTest) {
    this.fTest = fTest;
    return this;
  }
  /**
   * @return {@code this}.
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated AccessorsOuter setTest2(final String zTest2) {
    this.zTest2 = zTest2;
    return this;
  }
}
