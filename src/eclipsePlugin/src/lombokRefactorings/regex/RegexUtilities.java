package lombokRefactorings.regex;

import java.util.*;
import java.util.regex.*;

public class RegexUtilities {
	
	/**
	 * Finds all instances of the regular expression. I would like to define
	 * this in terms of findRegex, and return when findRegex throws an
	 * exception, but I heard this is not good practice.
	 * 
	 * @return Set of the precise strings found which contain all relevant info to the
	 *         matches.
	 * @author MaartenT
	 * @throws RegexNotFoundException
	 * @author MaartenT
	 */
	public static List<Matcher> findAllRegex(String regex, String source) {
		List<Matcher> matches = new ArrayList<Matcher>();
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		boolean found = false;
		int lastHit = -1;
		do {
			Matcher matcher = pattern.matcher(source);
			matcher.region(lastHit+1, source.length());
			if(matcher.find()){
				found = true;
				lastHit = matcher.start();
				matches.add(matcher);				
			}else{
				found=false;
			}
		} while(found);
		return matches;
	}

	/**
	 * Wrapper for findRegex/3
	 * 
	 * @param regex
	 * @param source
	 * @return
	 */
	public static Matcher findRegex(String regex, String source) {
		return findRegex(regex, source, 0);
	}

	/**
	 * Wrapper for findRegex/4
	 * 
	 * @param regex
	 * @param source
	 * @param start
	 * @return
	 */
	public static Matcher findRegex(String regex, String source, int start) {
		return findRegex(regex, source, start, source.length());
	}

	/**
	 * Returns both the first and last indices of the first occurrence of the
	 * regular expression pattern in the provided string.
	 * 
	 * @return Array is which the first integer is the first index and the
	 *         second integer the last index
	 * @author MaartenT
	 * @throws RegexNotFoundException
	 */
	public static Matcher findRegex(String regex, String source, int start,
			int end) {
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(source);
		try {
			matcher.region(start, end);
			if (!matcher.find()) {
				throw new RegexNotFoundException(regex);
			}
		} catch (RegexNotFoundException e) {
			e.printStackTrace();
		}

		return matcher;

	}

	public static class RegexNotFoundException extends Exception {
		private static final long serialVersionUID = 6146200786294862341L;

		public RegexNotFoundException(String regulargexpression) {
			super("Could not find tag: " + regulargexpression);
		}
	}
}
