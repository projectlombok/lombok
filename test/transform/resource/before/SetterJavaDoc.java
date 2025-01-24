import lombok.Setter;

class SetterJavaDoc {
	@Setter
	int noJavaDoc;
	
	/**
	 */
	@Setter
	int emptyJavaDoc;
	
	/**
	 * 
	 */
	@Setter
	int emptyJavaDocLine;
	
	/**
	 * Some text
	 */
	@Setter
	int oneLine;
	
	/**
	 * Some text
	 * in a second line
	 */
	@Setter
	int twoLines;
	
	/**
	 * Some text
	 * in a second line
	 * 
	 * And a second paragraph
	 */
	@Setter
	int secondParagraph;
	
	/**
	 * Some text
	 * in a second line
	 * <p>
	 * And a second paragraph
	 */
	@Setter
	int secondParagraphHtml;
	
	/**
	 * Some text
	 *
	 * @param withCustomParamTag some custom text
	 */
	@Setter
	int withCustomParamTag;
}