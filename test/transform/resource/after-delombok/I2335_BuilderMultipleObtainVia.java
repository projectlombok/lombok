public class I2335_BuilderMultipleObtainVia {
	private String theString;
	private Long theLong;
	public I2335_BuilderMultipleObtainVia(String theString, Long theLong) {
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
	@java.lang.SuppressWarnings("all")
	public static class I2335_BuilderMultipleObtainViaBuilder {
		@java.lang.SuppressWarnings("all")
		private String theString;
		@java.lang.SuppressWarnings("all")
		private Long theLong;
		@java.lang.SuppressWarnings("all")
		I2335_BuilderMultipleObtainViaBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder theString(final String theString) {
			this.theString = theString;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder theLong(final Long theLong) {
			this.theLong = theLong;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public I2335_BuilderMultipleObtainVia build() {
			return new I2335_BuilderMultipleObtainVia(this.theString, this.theLong);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder(theString=" + this.theString + ", theLong=" + this.theLong + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder builder() {
		return new I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder();
	}
	@java.lang.SuppressWarnings("all")
	public I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder toBuilder() {
		final String theString = this.getTheString();
		final Long theLong = this.getTheLong();
		return new I2335_BuilderMultipleObtainVia.I2335_BuilderMultipleObtainViaBuilder().theString(theString).theLong(theLong);
	}
}
