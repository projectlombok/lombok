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
package lombok.javac.handlers;

import lombok.core.LombokNode;
import lombok.util.LombokGeneratorHelper;
import lombok.util.LombokGeneratorHelper.ImplementationDetails;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Javac implementation details for the code generator
 *
 * @since Oct 5, 2014
 */
enum JavacImplementationDetails implements ImplementationDetails {

	INSTANCE;

	/* (non-Javadoc)
	 * @see lombok.util.LombokGeneratorHelper.ImplementationDetails#isStandard(lombok.core.LombokNode)
	 */
	@Override public boolean isStandard(final LombokNode<?, ?, ?> node) {
		return isFlagSet(Flags.STATIC, node);
	}

	/* (non-Javadoc)
	 * @see lombok.util.LombokGenerator.ImplementationDetails#isTransient(lombok.core.LombokNode)
	 */
	@Override public boolean isTransient(final LombokNode<?, ?, ?> node) {
		return isFlagSet(Flags.TRANSIENT, node);
	}

	/**
	 * @param flag
	 * @param child
	 * @return
	 */
	private static boolean isFlagSet(final int flag, final LombokNode<?, ?, ?> child) {
		return LombokGeneratorHelper.isFlagSet(flag, ((JCVariableDecl) child.get()).mods.flags);
	}

}
