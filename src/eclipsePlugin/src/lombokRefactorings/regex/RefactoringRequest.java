package lombokRefactorings.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import lombokRefactorings.regex.RegexUtilities.RegexNotFoundException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;

/**
 * Finds a comment in the source code which denotes a request for a specific
 * refactoring and then finds the corresponding refactoring, renames the tag
 * after performing the factoring. <br>
 * A request for a refactoring has the following format: <br>
 * /*n: RefactorName(parameters) :n*&#47; <br>
 * &nbsp&nbsp&nbsp content over which to perform the refactoring <br>
 * /*:n:*&#47;
 * 
 * @author PeterB & MaartenT
 * @param tagName
 *            : The number of the test, this is unique.
 * @throws CoreException
 * @param tagName
 *            part of the signature, so the program knows which closing closing
 *            tag belongs to which opening tag
 * @throws RegexNotFoundException
 */
public class RefactoringRequest {
	private static final String CLOSING_TAG_PREFIX = "/\\*\\s*?:\\s*?";
	private static final String CLOSING_TAG_SUFFIX = "\\s*?:\\s*?\\*/";

	private static final String OPENING_TAG_PREFIX = "/\\*\\s*?";
	private static final String OPENING_TAG_MIDDLE = ":(.*?):";
	private static final String OPENING_TAG_SUFFIX = "\\s*?\\*/";

	private static String openingTag;
	private static String closingTag;

	private final Matcher openingTagMatcher;
	private final Matcher closingTagMatcher;

	private final String refactoringName;
	private final String refactoringId;
	private final List<String> parameters;
	private final ICompilationUnit iCompilationUnit;

	public RefactoringRequest(String refactoringId,
			ICompilationUnit iCompilationUnit) throws RegexNotFoundException,
			CoreException {
		this.refactoringId = refactoringId;
		this.iCompilationUnit = iCompilationUnit;

		openingTag = OPENING_TAG_PREFIX + refactoringId + OPENING_TAG_MIDDLE
				+ refactoringId + OPENING_TAG_SUFFIX;
		closingTag = CLOSING_TAG_PREFIX + refactoringId + CLOSING_TAG_SUFFIX;

		openingTagMatcher = findLocationsOpeningTag(openingTag);
		closingTagMatcher = findLocationsClosingTag(closingTag);

		if (openingTagMatcher.group(1).trim().equalsIgnoreCase("done")
				|| openingTagMatcher.group(1).trim().toLowerCase()
						.startsWith("failed")) {
			// Refactoring has been done or failed already
			refactoringName = openingTagMatcher.group(1).trim();
			parameters = null;
		} else {
			Matcher nameAndParams = RegexUtilities.findRegex(
					"\\s*?(.*?)\\((.*?)\\)", iCompilationUnit.getSource(),
					openingTagMatcher.start(1), openingTagMatcher.end(1));
			refactoringName = iCompilationUnit.getSource()
					.substring(nameAndParams.start(1), nameAndParams.end(1))
					.trim();
			parameters = new ArrayList<String>();
			for (String param : nameAndParams.group(2).split(",")) {
				parameters.add(param.trim());
			}
		}
	}

	/**
	 * Returns matcher that contains start and end positions in the source code
	 * of an opening tag.
	 * 
	 * @param openingTag
	 * @return
	 * @throws JavaModelException
	 * @throws RegexNotFoundException
	 */
	private Matcher findLocationsOpeningTag(String openingTag)
			throws JavaModelException, RegexNotFoundException {
		return RegexUtilities.findRegex(openingTag,
				iCompilationUnit.getSource());
	}

	/**
	 * Returns matcher that contains start and end positions in the source code
	 * of a closing tag.
	 * 
	 * @param closingTag
	 * @return
	 * @throws JavaModelException
	 * @throws RegexNotFoundException
	 */
	private Matcher findLocationsClosingTag(String closingTag)
			throws JavaModelException, RegexNotFoundException {
		return RegexUtilities.findRegex(closingTag,
				iCompilationUnit.getSource());
	}

	public String getRefactoringName() {
		return refactoringName;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public String getParameter(int index) {
		return parameters.get(index);
	}

	/**
	 * Returns matcher that contains start and end positions in the source code
	 * of an opening tag.
	 */
	public Matcher getOpeningTagMatcher() {
		return openingTagMatcher;
	}

	/**
	 * Returns matcher that contains start and end positions in the source code
	 * of a closing tag.
	 */
	public Matcher getClosingTagMatcher() {
		return closingTagMatcher;
	}

	public ICompilationUnit getCompilationUnit() {
		return iCompilationUnit;
	}

	/**
	 * Find all tags in the source code of the compilation unit of tags that say
	 * <code>/*tagName here tagName*&#47;</code> and return their matchers,
	 * which containn start and end indices.
	 */
	public List<Matcher> findHereTags() throws JavaModelException {
		return RegexUtilities.findAllRegex("/\\*" + refactoringId
				+ "\\s*?(here)\\s*?" + refactoringId + "\\*/",
				iCompilationUnit.getSource());
	}

	/**
	 * Find all Java elements (fields, import declarations, etc) in the source
	 * code of the compilation unit following after a tag that says
	 * <code>/*tagName here tagName*&#47;</code>.
	 * 
	 * @return
	 * @throws JavaModelException
	 */
	public List<IJavaElement> findElements() throws JavaModelException {
		List<IJavaElement> elements = new ArrayList<IJavaElement>();
		List<Matcher> hereTags = findHereTags();
		for (Matcher hereTag : hereTags) {
			// First alphabetic character after the tag, should be the first
			// character of an element
			Matcher methodMatcher = RegexUtilities.findRegex("[a-zA-Z]",
					iCompilationUnit.getSource(), hereTag.end());
			IJavaElement toAdd = iCompilationUnit.getElementAt(methodMatcher
					.start());
			elements.add(toAdd);
		}
		return elements;
	}

	/**
	 * Find all members (fields, methods, etc) in the source code of the
	 * compilation unit following after a tag that says
	 * <code>/*tagName here tagName*&#47;</code>.
	 * 
	 * @return
	 * @throws JavaModelException
	 */
	public List<IMember> findMembers() throws JavaModelException {
		List<IMember> members = new ArrayList<IMember>();
		List<IJavaElement> elements = findElements();
		for (IJavaElement element : elements) {
			if (element instanceof IMember) {
				members.add((IMember) element);
			} else {
				System.err
						.println("This java element should be a member (like a field or a method): "
								+ element.getElementName()
								+ ". It has IJavaElement code "
								+ element.getElementType());
			}
		}
		return members;
	}

	/**
	 * Finds all methods in the source code of the compilation unit following
	 * after a tag that says <code>/*tagName here tagName*&#47;</code>.
	 * 
	 * @return
	 * @throws JavaModelException
	 */
	public List<IMethod> findMethods() throws JavaModelException {
		List<IMethod> methods = new ArrayList<IMethod>();
		List<IMember> members = findMembers();
		for (IMember member : members) {
			if (member instanceof IMethod) {
				methods.add((IMethod) member);
			} else {
				System.err.println("This element should be a method: "
						+ member.getElementName()
						+ ". It has IJavaElement code "
						+ member.getElementType() + " instead of "
						+ IJavaElement.METHOD);
			}
		}
		return methods;
	}

	/**
	 * Finds all Member types following the tag &#47;*n: here :n*&#47;, in which
	 * n is the tag name for the refactoring request.
	 * 
	 * @param indices
	 * @return
	 * @throws CoreException
	 * @author MaartenT & Saskia
	 */
	public List<IMember> findMembers(String tagName,
			ICompilationUnit iCompilationUnit) throws JavaModelException {
		int startMatching = 0;
		int end = iCompilationUnit.getSource().length();
		List<IMember> members = new ArrayList<IMember>();
		while (true) {
			try {
				Matcher matcher = RegexUtilities.findRegex("/\\*" + tagName
						+ ": here :" + tagName + "\\*/\\s*",
						iCompilationUnit.getSource(), startMatching, end);
				IJavaElement toAdd = iCompilationUnit.getElementAt(matcher
						.end());
				if (toAdd instanceof IMember) {
					members.add((IMember) toAdd);
				}
				startMatching = matcher.end();
			} catch (Exception e) {
				break;
			}
		}
		return members;
	}

	public IJavaElement[] getElementsArray() throws JavaModelException {
		return findElements().toArray(new IJavaElement[0]);
	}

	public IMember[] getMembersArray() throws JavaModelException {
		return findMembers().toArray(new IMember[0]);
	}

	public IMethod[] getMethodsArray() throws JavaModelException {
		return findMethods().toArray(new IMethod[0]);
	}
}