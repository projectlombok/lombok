import lombok.*;
import lombokRefactorings.regex.RegexUtilities;
public class Sandbox {
	
	static final int target = 0;
	
	public static void main(String[] args) {
		String source = "/*1: hallo() :1*/";
		String regex = "/\\*\\s*?1\\s*?:(.*?\\)).*?:\\s*?1\\s*?\\*/";
		RegexUtilities.findRegex(regex, source);
		
	}

}