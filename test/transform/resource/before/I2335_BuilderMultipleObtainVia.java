import lombok.Builder;

@Builder
public class I2335_BuilderMultipleObtainVia {
	private String theString;
	private Long theLong;

	@Builder(toBuilder = true)
	public I2335_BuilderMultipleObtainVia(
		@Builder.ObtainVia(method = "getTheString") String theString,
		@Builder.ObtainVia(method = "getTheLong") Long theLong
	) {
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
}
