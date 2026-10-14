class SetterJavaDoc {
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
	int withCustomParamTag;

	@java.lang.SuppressWarnings("all")
	public void setNoJavaDoc(final int noJavaDoc) {
		this.noJavaDoc = noJavaDoc;
	}

	/**
	 */
	@java.lang.SuppressWarnings("all")
	public void setEmptyJavaDoc(final int emptyJavaDoc) {
		this.emptyJavaDoc = emptyJavaDoc;
	}

	/**
	 */
	@java.lang.SuppressWarnings("all")
	public void setEmptyJavaDocLine(final int emptyJavaDocLine) {
		this.emptyJavaDocLine = emptyJavaDocLine;
	}

	/**
	 * Some text
	 * @param oneLine Some text
	 */
	@java.lang.SuppressWarnings("all")
	public void setOneLine(final int oneLine) {
		this.oneLine = oneLine;
	}

	/**
	 * Some text
	 * in a second line
	 * @param twoLines Some text
	 * in a second line
	 */
	@java.lang.SuppressWarnings("all")
	public void setTwoLines(final int twoLines) {
		this.twoLines = twoLines;
	}

	/**
	 * Some text
	 * in a second line
	 * 
	 * And a second paragraph
	 * @param secondParagraph Some text
	 * in a second line
	 */
	@java.lang.SuppressWarnings("all")
	public void setSecondParagraph(final int secondParagraph) {
		this.secondParagraph = secondParagraph;
	}

	/**
	 * Some text
	 * in a second line
	 * <p>
	 * And a second paragraph
	 * @param secondParagraphHtml Some text
	 * in a second line
	 */
	@java.lang.SuppressWarnings("all")
	public void setSecondParagraphHtml(final int secondParagraphHtml) {
		this.secondParagraphHtml = secondParagraphHtml;
	}

	/**
	 * Some text
	 *
	 * @param withCustomParamTag some custom text
	 */
	@java.lang.SuppressWarnings("all")
	public void setWithCustomParamTag(final int withCustomParamTag) {
		this.withCustomParamTag = withCustomParamTag;
	}
}
