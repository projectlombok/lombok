/*
 * Copyright (C) 2014 The Project Lombok Authors.
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
package lombok.eclipse.handlers;

import lombok.core.LombokNode;
import lombok.util.LombokGeneratorHelper;
import lombok.util.LombokGeneratorHelper.ImplementationDetails;

import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Javac implementation details for the code generator
 *
 * @since Oct 5, 2014
 */
enum EclipseImplementationDetails implements ImplementationDetails {

	INSTANCE;

	/* (non-Javadoc)
	 * @see lombok.util.LombokGeneratorHelper.ImplementationDetails#isStandard(lombok.core.LombokNode)
	 */
	@Override public boolean isStandard(final LombokNode<?, ?, ?> node) {
		return isFlagSet(ClassFileConstants.AccStatic, node);
	}

	/* (non-Javadoc)
	 * @see lombok.util.LombokGenerator.ImplementationDetails#isTransient(lombok.core.LombokNode)
	 */
	@Override public boolean isTransient(final LombokNode<?, ?, ?> node) {
		return isFlagSet(ClassFileConstants.AccTransient, node);
	}

	/**
	 * @param flag
	 * @param child
	 * @return
	 */
	private static boolean isFlagSet(final int flag, final LombokNode<?, ?, ?> child) {
		return LombokGeneratorHelper.isFlagSet(flag, ((FieldDeclaration) child.get()).modifiers);
	}

}
