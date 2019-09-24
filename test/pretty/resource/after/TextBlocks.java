public class TextBlocks {
	private String example = """
		This should not be indented.
		line 2
		line 3
		""";
	
	private String ex2 = """
			This should be though.
		""";
	
	private String ex3 = "This is a simple\nstring";
	
	private String ex4 = """
		""";
	
	private String bizarroDent = """
		foo
		    bar""" + "hey" + """
		weird!""";
	
	private String stringFolding = "hmmm" + """
		line1
			line2""";
}
