/*
 * Copyright (C) 2026 The Project Lombok Authors.
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

import static org.junit.Assert.*;

import java.util.Locale;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.junit.Test;

import lombok.core.AST.Kind;
import lombok.eclipse.handlers.EclipseHandlerUtil;

/**
 * Regression for issue #4051: {@code isClass} must not treat non-{@code TypeDeclaration}
 * nodes (e.g. JDT {@code CompletionOnFieldType}, which extends {@code FieldDeclaration}) as classes.
 * An incorrect {@code true} causes handlers such as {@code HandleValue} to unguardedly cast and crash.
 */
public class TestEclipseIsClass {
	@Test
	public void isClassIsFalseForFieldDeclarationNodes() {
		CompilationUnitDeclaration cud = newCompilationUnit();
		TypeDeclaration type = new TypeDeclaration(cud.compilationResult);
		type.name = "Example".toCharArray();
		type.modifiers = ClassFileConstants.AccPublic;
		
		FieldDeclaration field = new FieldDeclaration();
		field.name = "field".toCharArray();
		field.type = TypeReference.baseTypeReference(TypeReference.T_int, 0);
		type.fields = new FieldDeclaration[] {field};
		cud.types = new TypeDeclaration[] {type};
		
		EclipseAST ast = new EclipseAST(cud);
		EclipseNode typeNode = null;
		EclipseNode fieldNode = null;
		for (EclipseNode child : ast.top().down()) {
			if (child.getKind() == Kind.TYPE) {
				typeNode = child;
				for (EclipseNode member : child.down()) {
					if (member.getKind() == Kind.FIELD) {
						fieldNode = member;
						break;
					}
				}
			}
		}
		
		assertNotNull(typeNode);
		assertNotNull(fieldNode);
		assertTrue("plain class should be considered a class", EclipseHandlerUtil.isClass(typeNode));
		assertFalse(
			"field nodes (including CompletionOnFieldType) must not be considered classes — issue #4051",
			EclipseHandlerUtil.isClass(fieldNode));
	}
	
	@Test
	public void isClassIsFalseForInterface() {
		CompilationUnitDeclaration cud = newCompilationUnit();
		TypeDeclaration type = new TypeDeclaration(cud.compilationResult);
		type.name = "I".toCharArray();
		type.modifiers = ClassFileConstants.AccInterface;
		cud.types = new TypeDeclaration[] {type};
		
		EclipseAST ast = new EclipseAST(cud);
		EclipseNode typeNode = ast.top().down().iterator().next();
		assertFalse(EclipseHandlerUtil.isClass(typeNode));
	}
	
	private static CompilationUnitDeclaration newCompilationUnit() {
		CompilerOptions options = new CompilerOptions();
		ProblemReporter problemReporter = new ProblemReporter(
			DefaultErrorHandlingPolicies.exitOnFirstError(),
			options,
			new DefaultProblemFactory(Locale.ENGLISH));
		CompilationResult result = new CompilationResult("Example.java".toCharArray(), 0, 1, 100);
		return new CompilationUnitDeclaration(problemReporter, result, 0);
	}
}
