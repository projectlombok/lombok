import lombok.Getter;

/*bla */
public class CommentsInterspersed {
	/** javadoc for field */
	private int x;
	
	/* bla2 */
	@Getter()
	private String test = "foo";
	//$NON-NLS-1$
	
	/** Javadoc on method */
	public native void gwtTest();
	/*-{
		javascript;
	}-*/
	
	public CommentsInterspersed() {
	}
	
	public String getTest() {
		return this.test;
	}
}
//haha!
