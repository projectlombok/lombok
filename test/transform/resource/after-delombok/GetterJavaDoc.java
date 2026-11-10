class GetterJavaDoc {
	int noJavaDoc;
	/**
	 */
	int emptyJavaDoc;
	/**
	 */
	int emptyJavaDocLine;
	/**
	 * Some text
	 */
	int oneLine;
	/**
	 * Some text
	 * in a second line
	 */
	int twoLines;
	/**
	 * Some text
	 * in a second line
	 * 
	 * And a second paragraph
	 */
	int secondParagraph;
	/**
	 * Some text
	 * in a second line
	 * <p>
	 * And a second paragraph
	 */
	int secondParagraphHtml;
	/**
	 * Some text
	 */
	int withCustomReturnTag;

	@java.lang.SuppressWarnings("all")
	public int getNoJavaDoc() {
		return this.noJavaDoc;
	}

	/**
	 */
	@java.lang.SuppressWarnings("all")
	public int getEmptyJavaDoc() {
		return this.emptyJavaDoc;
	}

	/**
	 */
	@java.lang.SuppressWarnings("all")
	public int getEmptyJavaDocLine() {
		return this.emptyJavaDocLine;
	}

	/**
	 * Some text
	 * @return Some text
	 */
	@java.lang.SuppressWarnings("all")
	public int getOneLine() {
		return this.oneLine;
	}

	/**
	 * Some text
	 * in a second line
	 * @return Some text
	 * in a second line
	 */
	@java.lang.SuppressWarnings("all")
	public int getTwoLines() {
		return this.twoLines;
	}

	/**
	 * Some text
	 * in a second line
	 * 
	 * And a second paragraph
	 * @return Some text
	 * in a second line
	 */
	@java.lang.SuppressWarnings("all")
	public int getSecondParagraph() {
		return this.secondParagraph;
	}

	/**
	 * Some text
	 * in a second line
	 * <p>
	 * And a second paragraph
	 * @return Some text
	 * in a second line
	 */
	@java.lang.SuppressWarnings("all")
	public int getSecondParagraphHtml() {
		return this.secondParagraphHtml;
	}

	/**
	 * Some text
	 *
	 * @return some custom text
	 */
	@java.lang.SuppressWarnings("all")
	public int getWithCustomReturnTag() {
		return this.withCustomReturnTag;
	}
}
