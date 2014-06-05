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
package lombok.eclipse;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;

import lombok.core.FieldAugment;

public final class EclipseAugments {
	private EclipseAugments() {
		// Prevent instantiation
	}
	
	public static final FieldAugment<FieldDeclaration, Boolean> FieldDeclaration_booleanLazyGetter = FieldAugment.augment(FieldDeclaration.class, boolean.class, "lombok$booleanLazyGetter");
	public static final FieldAugment<ASTNode, Boolean> ASTNode_handled = FieldAugment.augment(ASTNode.class, boolean.class, "lombok$handled");
	public static final FieldAugment<ASTNode, ASTNode> ASTNode_generatedBy = FieldAugment.augment(ASTNode.class, ASTNode.class, "$generatedBy");
	public static final FieldAugment<Annotation, Boolean> Annotation_applied = FieldAugment.augment(Annotation.class, boolean.class, "lombok$applied");
}
