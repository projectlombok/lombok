/*
 * Copyright (C) 2012-2021 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.Arrays;
import java.util.List;

import lombok.core.AST;
import lombok.eclipse.*;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.ExtensionMethod;
import lombok.spi.Provides;

// This handler just does some additional error checking; the real work is done in the agent.
@Provides(EclipseASTVisitor.class)
@HandlerPriority(66560) // 2^16 + 2^10; we must run AFTER HandleVal which is at 2^16
public class HandleExtensionMethod extends EclipseASTAdapter {

	private static final char[] EXTENSION_METHOD = "ExtensionMethod".toCharArray();

	@Override public void visitType(EclipseNode typeNode, TypeDeclaration typeDecl) {
		int modifiers = typeDecl.modifiers;
		boolean notAClass = (modifiers & (ClassFileConstants.AccAnnotation)) != 0;

		AnnotationValues<ExtensionMethod> extensionMethod = null;
		EclipseNode source = typeNode;

		List<Object> listenerInterfaces = null;
		boolean suppressBaseMethodsIsExplicit = false;
		ExtensionMethod em = null;
		for (EclipseNode jn : typeNode.down()) {
			if (jn.getKind() != AST.Kind.ANNOTATION) continue;
			Annotation ann = (Annotation) jn.get();
			TypeReference typeTree = ann.type;
			if (typeTree == null) continue;
			if (typeTree instanceof SingleTypeReference) {
				char[] t = ((SingleTypeReference) typeTree).token;
				if (!Arrays.equals(t, EXTENSION_METHOD)) continue;
			} else if (typeTree instanceof QualifiedTypeReference) {
				char[][] t = ((QualifiedTypeReference) typeTree).tokens;
				if (!Eclipse.nameEquals(t, "lombok.experimental.ExtensionMethod")) continue;
			} else {
				continue;
			}

			if (!typeMatches(ExtensionMethod.class, jn, typeTree)) continue;

			source = jn;
			extensionMethod = createAnnotation(ExtensionMethod.class, jn);
			suppressBaseMethodsIsExplicit = extensionMethod.isExplicit("suppressBaseMethods");

			handleExperimentalFlagUsage(jn, ConfigurationKeys.EXTENSION_METHOD_FLAG_USAGE, "@ExtensionMethod");

			em = extensionMethod.getInstance();
			if (notAClass) {
				jn.addError("@ExtensionMethod is legal only on classes and enums and interfaces.");
				return;
			}

			listenerInterfaces = extensionMethod.getActualExpressions("value");
			if (listenerInterfaces.isEmpty()) {
				jn.addWarning("@ExtensionMethod has no effect since no extension types were specified.");
				return;
			}
			break;
		}

	}
}
