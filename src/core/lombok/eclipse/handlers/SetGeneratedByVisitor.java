/*
 * Copyright (C) 2011-2015 The Project Lombok Authors.
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

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArgumentExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocArraySingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocImplicitTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.ast.JavadocQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public final class SetGeneratedByVisitor extends ASTVisitor {
	private static final long INT_TO_LONG_MASK = 0x00000000FFFFFFFFL;
	
	private final ASTNode source;
	private final int sourceStart;
	private final int sourceEnd;
	private final long sourcePos;
	
	public SetGeneratedByVisitor(ASTNode source) {
		this.source = source;
		this.sourceStart = this.source.sourceStart;
		this.sourceEnd = this.source.sourceEnd;
		this.sourcePos = (long)sourceStart << 32 | (sourceEnd & INT_TO_LONG_MASK);
	}
	
	private void fixPositions(JavadocAllocationExpression node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.memberStart = sourceStart;
		node.tagSourceEnd = sourceEnd;
		node.tagSourceStart = sourceStart;
	}
	
	private void fixPositions(JavadocMessageSend node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.nameSourcePosition = sourcePos;
		node.tagSourceEnd = sourceEnd;
		node.tagSourceStart = sourceStart;
	}
	
	private void fixPositions(JavadocSingleNameReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.tagSourceEnd = sourceEnd;
		node.tagSourceStart = sourceStart;
	}
	
	private void fixPositions(JavadocSingleTypeReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.tagSourceEnd = sourceEnd;
		node.tagSourceStart = sourceStart;
	}

	private void fixPositions(JavadocFieldReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.nameSourcePosition = sourcePos;
		node.tagSourceEnd = sourceEnd;
		node.tagSourceStart = sourceStart;
	}
	
	private void fixPositions(JavadocArrayQualifiedTypeReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		if (node.sourcePositions == null || node.sourcePositions.length != node.tokens.length) node.sourcePositions = new long[node.tokens.length];
		Arrays.fill(node.sourcePositions, sourcePos);
		node.tagSourceEnd = sourceEnd;
		node.tagSourceStart = sourceStart;
	}
	
	private void fixPositions(JavadocQualifiedTypeReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		if (node.sourcePositions == null || node.sourcePositions.length != node.tokens.length) node.sourcePositions = new long[node.tokens.length];
		Arrays.fill(node.sourcePositions, sourcePos);
		node.tagSourceEnd = sourceEnd;
		node.tagSourceStart = sourceStart;
	}
	
	private void fixPositions(Annotation node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.declarationSourceEnd = sourceEnd;
	}

	private void fixPositions(ArrayTypeReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.originalSourceEnd = sourceEnd;
	}
	
	private void fixPositions(AbstractMethodDeclaration node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.bodyEnd = sourceEnd;
		node.bodyStart = sourceStart;
		node.declarationSourceEnd = sourceEnd;
		node.declarationSourceStart = sourceStart;
		node.modifiersSourceStart = sourceStart;
	}
	
	private void fixPositions(Javadoc node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.valuePositions = sourceStart;
	}

	private void fixPositions(Initializer node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.declarationEnd = sourceEnd;
		node.declarationSourceEnd = sourceEnd;
		node.declarationSourceStart = sourceStart;
		node.modifiersSourceStart = sourceStart;
		node.endPart1Position = sourceEnd;
		node.endPart2Position = sourceEnd;
		node.bodyStart = sourceStart;
		node.bodyEnd = sourceEnd;
	}

	private void fixPositions(TypeDeclaration node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.bodyEnd = sourceEnd;
		node.bodyStart = sourceStart;
		node.declarationSourceEnd = sourceEnd;
		node.declarationSourceStart = sourceStart;
		node.modifiersSourceStart = sourceStart;
	}
	
	private void fixPositions(ImportReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.declarationEnd = sourceEnd;
		node.declarationSourceEnd = sourceEnd;
		node.declarationSourceStart = sourceStart;
		if (node.sourcePositions == null || node.sourcePositions.length != node.tokens.length) node.sourcePositions = new long[node.tokens.length];
		Arrays.fill(node.sourcePositions, sourcePos);
	}
	
	private void fixPositions(ASTNode node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
	}
	
	private void fixPositions(SwitchStatement node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.blockStart = sourceStart;
	}
	
	private void fixPositions(Expression node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
	}
	
	private void fixPositions(AbstractVariableDeclaration node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.declarationEnd = sourceEnd;
		node.declarationSourceEnd = sourceEnd;
		node.declarationSourceStart = sourceStart;
		node.modifiersSourceStart = sourceStart;
	}
		
	private void fixPositions(FieldDeclaration node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.declarationEnd = sourceEnd;
		node.declarationSourceEnd = sourceEnd;
		node.declarationSourceStart = sourceStart;
		node.modifiersSourceStart = sourceStart;
		node.endPart1Position = sourceEnd;
		node.endPart2Position = sourceEnd;
	}

	private void fixPositions(FieldReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.nameSourcePosition = sourcePos;
	}
	
	private void fixPositions(MessageSend node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		node.nameSourcePosition = sourcePos;
	}
	
	private void fixPositions(QualifiedNameReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		if (node.sourcePositions == null || node.sourcePositions.length != node.tokens.length) node.sourcePositions = new long[node.tokens.length];
		Arrays.fill(node.sourcePositions, sourcePos);
	}
	
	private void fixPositions(QualifiedTypeReference node) {
		node.sourceEnd = sourceEnd;
		node.sourceStart = sourceStart;
		node.statementEnd = sourceEnd;
		if (node.sourcePositions == null || node.sourcePositions.length != node.tokens.length) node.sourcePositions = new long[node.tokens.length];
		Arrays.fill(node.sourcePositions, sourcePos);
	}
	
	@Override public boolean visit(AllocationExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(AND_AND_Expression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(AnnotationMethodDeclaration node, ClassScope classScope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, classScope);
	}

	@Override public boolean visit(Argument node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}

	@Override public boolean visit(Argument node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayAllocationExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayInitializer node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayQualifiedTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayQualifiedTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(AssertStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}

	@Override public boolean visit(Assignment node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(BinaryExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Block node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(BreakStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CaseStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CastExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CharLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ClassLiteralAccess node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Clinit node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CompilationUnitDeclaration node, CompilationUnitScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}

	@Override public boolean visit(CompoundAssignment node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ConditionalExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ConstructorDeclaration node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ContinueStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(DoStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(DoubleLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(EmptyStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(EqualExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ExplicitConstructorCall node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ExtendedStringLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FalseLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FieldDeclaration node, MethodScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FieldReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FieldReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FloatLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ForeachStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ForStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(IfStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ImportReference node, CompilationUnitScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Initializer node, MethodScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(InstanceOfExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(IntLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Javadoc node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}

	@Override public boolean visit(Javadoc node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocAllocationExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocAllocationExpression node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArgumentExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArgumentExpression node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArrayQualifiedTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArrayQualifiedTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArraySingleTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArraySingleTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocFieldReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocFieldReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocImplicitTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocImplicitTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocMessageSend node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocMessageSend node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocQualifiedTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocQualifiedTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocReturnStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocReturnStatement node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleNameReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleNameReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(LabeledStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(LocalDeclaration node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(LongLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MarkerAnnotation node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MemberValuePair node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MessageSend node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MethodDeclaration node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(StringLiteralConcatenation node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(NormalAnnotation node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(NullLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(OR_OR_Expression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedQualifiedTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedQualifiedTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedSingleTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedSingleTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(PostfixExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(PrefixExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedAllocationExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedNameReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedNameReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedSuperReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedSuperReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedThisReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedThisReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ReturnStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleMemberAnnotation node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleNameReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleNameReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleTypeReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleTypeReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(StringLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SuperReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SwitchStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SynchronizedStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ThisReference node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ThisReference node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ThrowStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TrueLiteral node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TryStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeDeclaration node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeDeclaration node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeDeclaration node, CompilationUnitScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeParameter node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeParameter node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(UnaryExpression node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(WhileStatement node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Wildcard node, BlockScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Wildcard node, ClassScope scope) {
		fixPositions(setGeneratedBy(node, source));
		return super.visit(node, scope);
	}

// missing methods
//	public boolean visit(MarkerAnnotation node, ClassScope scope){ return true;}
//	public boolean visit(MemberValuePair node, ClassScope scope){ return true;}
//	public boolean visit(NormalAnnotation node, ClassScope scope){ return true;}
//	public boolean visit(SingleMemberAnnotation node, ClassScope scope){ return true;}
	
// missing methods from later versions
//	public boolean visit(UnionTypeReference node, BlockScope scope){ return true;}
//	public boolean visit(UnionTypeReference node, ClassScope scope){ return true;}
//	public boolean visit(LambdaExpression node, BlockScope scope){ return true;}
//	public boolean visit(ReferenceExpression node, BlockScope scope){ return true;}
//	public boolean visit(IntersectionCastTypeReference node, ClassScope scope){ return true;}
//	public boolean visit(IntersectionCastTypeReference node, BlockScope scope){ return true;}
}