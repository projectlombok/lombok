/*
 * Copyright (C) 2012-2019 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.agent;

public class PatchDiagnostics {
	/**
	 * 
	 * org.eclipse.jdt.core.dom.ASTNode#setSourceRange starts off doing some sanity checks on the input arguments, and, if these fail the sanity check,
	 * a {@code IllegalArgumentException} is thrown. However, these IAEs do not have any message and are thus pretty much useless. We do the exact same
	 * checks, and throw the exact same exception (thus, effectively, we don't change how eclipse operates), but, we <em>do</em> provide a useful message.
	 */
	public static boolean setSourceRangeCheck(Object astNode, int startPosition, int length) {
		String nodeTxt;
		if (startPosition >= 0 && length < 0) {
			if (astNode == null) nodeTxt = "(NULL NODE)";
			else nodeTxt = astNode.getClass() + ": " + astNode.toString();
			throw new IllegalArgumentException("startPos = " + startPosition + " and length is " + length + ".\n" +
				"This breaks the rule that lengths are not allowed to be negative. Affected Node:\n" + nodeTxt);
		}
		
		if (startPosition < 0 && length != 0) {
			if (astNode == null) nodeTxt = "(NULL NODE)";
			else nodeTxt = astNode.getClass() + ": " + astNode.toString();
			throw new IllegalArgumentException("startPos = " + startPosition + " and length is " + length + ".\n" +
				"This breaks the rule that length must be 0 if startPosition is negative. Affected Node:\n" + nodeTxt);
		}
		
		return false;
	}
}
