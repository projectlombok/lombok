/*
 * Copyright © 2010 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import lombok.val;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseASTAdapter;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.mangosdk.spi.ProviderFor;

/*
 * This class just handles 2 basic error cases. The real meat of eclipse 'val' support is in {@code EclipsePatcher}'s {@code patchHandleVal} method.
 */
@ProviderFor(EclipseASTVisitor.class)
public class HandleVal extends EclipseASTAdapter {
	@Override public void visitLocal(EclipseNode localNode, LocalDeclaration local) {
		if (!Eclipse.typeMatches(val.class, localNode, local.type)) return;
		boolean variableOfForEach = false;
		
		if (localNode.directUp().get() instanceof ForeachStatement) {
			ForeachStatement fs = (ForeachStatement) localNode.directUp().get();
			variableOfForEach = fs.elementVariable == local;
		}
		
		if (local.initialization == null && !variableOfForEach) {
			localNode.addError("'val' on a local variable requires an initializer expression");
			return;
		}
		
		if (local.initialization instanceof ArrayInitializer) {
			localNode.addError("'val' is not compatible with array initializer expressions. Use the full form (new int[] { ... } instead of just { ... })");
			return;
		}
	}
}
