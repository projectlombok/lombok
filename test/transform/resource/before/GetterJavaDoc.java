import lombok.Getter;

class GetterJavaDoc {
	@Getter
	int noJavaDoc;
	
	/**
	 */
	@Getter
	int emptyJavaDoc;
	
	/**
	 * 
	 */
	@Getter
	int emptyJavaDocLine;
	
	/**
	 * Some text
	 */
	@Getter
	int oneLine;
	
	/**
	 * Some text
	 * in a second line
	 */
	@Getter
	int twoLines;
	
	/**
	 * Some text
	 * in a second line
	 * 
	 * And a second paragraph
	 */
	@Getter
	int secondParagraph;
	
	/**
	 * Some text
	 * in a second line
	 * <p>
	 * And a second paragraph
	 */
	@Getter
	int secondParagraphHtml;
	
	/**
	 * Some text
	 *
	 * @return some custom text
	 */
	@Getter
	int withCustomReturnTag;
}