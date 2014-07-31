/*
 * Copyright (C) 2013-2014 The Project Lombok Authors.
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
package lombok.core.handlers;

import lombok.core.JavaIdentifiers;
import lombok.core.LombokNode;

public class HandlerUtil {
	private HandlerUtil() {}

	public static int primeForHashcode() {
		return 59;
	}

	public static int primeForTrue() {
		return 79;
	}

	public static int primeForFalse() {
		return 97;
	}

	/** Checks if the given name is a valid identifier.
	 *
	 * If it is, this returns {@code true} and does nothing else.
	 * If it isn't, this returns {@code false} and adds an error message to the supplied node.
	 */
	public static boolean checkName(String nameSpec, String identifier, LombokNode<?, ?, ?> errorNode) {
		if (identifier.isEmpty()) {
			errorNode.addError(nameSpec + " cannot be the empty string.");
			return false;
		}

		if (!JavaIdentifiers.isValidJavaIdentifier(identifier)) {
			errorNode.addError(nameSpec + " must be a valid java identifier.");
			return false;
		}

		return true;
	}

}
