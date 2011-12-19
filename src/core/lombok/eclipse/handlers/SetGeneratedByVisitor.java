package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.EclipseHandlerUtil.setGeneratedBy;

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
	private final int newSourceStart;
	private final int newSourceEnd;
	
	public SetGeneratedByVisitor(ASTNode source) {
		this.source = source;
		this.newSourceStart = this.source.sourceStart;
		this.newSourceEnd = this.source.sourceEnd;
	}
	
	private void applyOffset(JavadocAllocationExpression node) {
		applyOffsetExpression(node);
		node.memberStart = newSourceStart;
		node.tagSourceEnd = newSourceEnd;
		node.tagSourceStart = newSourceStart;
	}
	
	private void applyOffset(JavadocMessageSend node) {
		applyOffsetMessageSend(node);
		node.tagSourceEnd = newSourceEnd;
		node.tagSourceStart = newSourceStart;
	}
	
	private void applyOffset(JavadocSingleNameReference node) {
		applyOffsetExpression(node);
		node.tagSourceEnd = newSourceEnd;
		node.tagSourceStart = newSourceStart;
	}
	
	private void applyOffset(JavadocSingleTypeReference node) {
		applyOffsetExpression(node);
		node.tagSourceEnd = newSourceEnd;
		node.tagSourceStart = newSourceStart;
	}

	private void applyOffset(JavadocFieldReference node) {
		applyOffsetFieldReference(node);
		node.tagSourceEnd = newSourceEnd;
		node.tagSourceStart = newSourceStart;
	}
	
	private void applyOffset(JavadocArrayQualifiedTypeReference node) {
		applyOffsetQualifiedTypeReference(node);
		node.tagSourceEnd = newSourceEnd;
		node.tagSourceStart = newSourceStart;
	}
	
	private void applyOffset(JavadocQualifiedTypeReference node) {
		applyOffsetQualifiedTypeReference(node);
		node.tagSourceEnd = newSourceEnd;
		node.tagSourceStart = newSourceStart;
	}
	
	private void applyOffset(Annotation node) {
		applyOffsetExpression(node);
		node.declarationSourceEnd = newSourceEnd;
	}

	private void applyOffset(ArrayTypeReference node) {
		applyOffsetExpression(node);
		node.originalSourceEnd = newSourceEnd;
	}
	
	private void applyOffset(AbstractMethodDeclaration node) {
		applyOffsetASTNode(node);
		node.bodyEnd = newSourceEnd;
		node.bodyStart = newSourceStart;
		node.declarationSourceEnd = newSourceEnd;
		node.declarationSourceStart = newSourceStart;
		node.modifiersSourceStart = newSourceStart;
	}
	
	private void applyOffset(Javadoc node) {
		applyOffsetASTNode(node);
		node.valuePositions = newSourceStart;
		for (int i = 0; i < node.inheritedPositions.length; i++) {
			node.inheritedPositions[i] = recalcSourcePosition(node.inheritedPositions[i]);
		}
	}

	private void applyOffset(Initializer node) {
		applyOffsetFieldDeclaration(node);
		node.bodyStart = newSourceStart;
		node.bodyEnd = newSourceEnd;
	}

	private void applyOffset(TypeDeclaration node) {
		applyOffsetASTNode(node);
		node.bodyEnd = newSourceEnd;
		node.bodyStart = newSourceStart;
		node.declarationSourceEnd = newSourceEnd;
		node.declarationSourceStart = newSourceStart;
		node.modifiersSourceStart = newSourceStart;
	}
	
	private void applyOffset(ImportReference node) {
		applyOffsetASTNode(node);
		node.declarationEnd = newSourceEnd;
		node.declarationSourceEnd = newSourceEnd;
		node.declarationSourceStart = newSourceStart;
		for (int i = 0; i < node.sourcePositions.length; i++) {
			node.sourcePositions[i] = recalcSourcePosition(node.sourcePositions[i]);
		}		
	}

	private void applyOffsetASTNode(ASTNode node) {
		node.sourceEnd = newSourceEnd;
		node.sourceStart = newSourceStart;
	}

	private void applyOffsetExpression(Expression node) {
		applyOffsetASTNode(node);
//		if (node.statementEnd != -1) {
			node.statementEnd = newSourceEnd;
//		}
	}

	private void applyOffsetVariable(AbstractVariableDeclaration node) {
		applyOffsetASTNode(node);
		node.declarationEnd = newSourceEnd;
		node.declarationSourceEnd = newSourceEnd;
		node.declarationSourceStart = newSourceStart;
		node.modifiersSourceStart = newSourceStart;
	}
		
	private void applyOffsetFieldDeclaration(FieldDeclaration node) {
		applyOffsetVariable(node);
		node.endPart1Position = newSourceEnd;
		node.endPart2Position = newSourceEnd;
	}

	private void applyOffsetFieldReference(FieldReference node) {
		applyOffsetExpression(node);
		node.nameSourcePosition = recalcSourcePosition(node.nameSourcePosition);
	}
	
	private void applyOffsetMessageSend(MessageSend node) {
		applyOffsetExpression(node);
		node.nameSourcePosition = recalcSourcePosition(node.nameSourcePosition);
	}
	
	private void applyOffsetQualifiedTypeReference(QualifiedTypeReference node) {
		applyOffsetExpression(node);
		for (int i = 0; i < node.sourcePositions.length; i++) {
			node.sourcePositions[i] = recalcSourcePosition(node.sourcePositions[i]);
		}
	}

	/** See {@link FieldReference#nameSourcePosition} for explanation */
	private long recalcSourcePosition(long sourcePosition) {
//		long start = (sourcePosition >>> 32);
//		long end = (sourcePosition & 0x00000000FFFFFFFFL);
//		start = newSourceStart;
//		end = newSourceStart;
//		return ((start<<32)+end); 
		return ((long)newSourceStart << 32) | (newSourceStart & INT_TO_LONG_MASK);
	}
	
	@Override public boolean visit(AllocationExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}

	@Override public boolean visit(AND_AND_Expression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(AnnotationMethodDeclaration node, ClassScope classScope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, classScope);
	}

	@Override public boolean visit(Argument node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetVariable(node);
		return super.visit(node, scope);
	}

	@Override public boolean visit(Argument node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetVariable(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayAllocationExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayInitializer node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayQualifiedTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetQualifiedTypeReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayQualifiedTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetQualifiedTypeReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ArrayTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(AssertStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}

	@Override public boolean visit(Assignment node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(BinaryExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Block node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(BreakStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CaseStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CastExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CharLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ClassLiteralAccess node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Clinit node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(CompilationUnitDeclaration node, CompilationUnitScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}

	@Override public boolean visit(CompoundAssignment node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ConditionalExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ConstructorDeclaration node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ContinueStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(DoStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(DoubleLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(EmptyStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(EqualExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ExplicitConstructorCall node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ExtendedStringLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FalseLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FieldDeclaration node, MethodScope scope) {
		setGeneratedBy(node, source);
		applyOffsetFieldDeclaration(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FieldReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetFieldReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FieldReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetFieldReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(FloatLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ForeachStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ForStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(IfStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ImportReference node, CompilationUnitScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Initializer node, MethodScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(InstanceOfExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(IntLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Javadoc node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}

	@Override public boolean visit(Javadoc node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocAllocationExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocAllocationExpression node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArgumentExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArgumentExpression node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArrayQualifiedTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArrayQualifiedTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArraySingleTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocArraySingleTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocFieldReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocFieldReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocImplicitTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocImplicitTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocMessageSend node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocMessageSend node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocQualifiedTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocQualifiedTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocReturnStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocReturnStatement node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleNameReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleNameReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(JavadocSingleTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(LabeledStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(LocalDeclaration node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetVariable(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(LongLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MarkerAnnotation node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MemberValuePair node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MessageSend node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetMessageSend(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(MethodDeclaration node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(StringLiteralConcatenation node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(NormalAnnotation node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(NullLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(OR_OR_Expression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedQualifiedTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetQualifiedTypeReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedQualifiedTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetQualifiedTypeReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedSingleTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ParameterizedSingleTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(PostfixExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(PrefixExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedAllocationExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedNameReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedNameReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedSuperReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedSuperReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedThisReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedThisReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetQualifiedTypeReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(QualifiedTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetQualifiedTypeReference(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ReturnStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleMemberAnnotation node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleNameReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleNameReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleTypeReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SingleTypeReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(StringLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SuperReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SwitchStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		node.blockStart = newSourceStart;
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(SynchronizedStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ThisReference node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ThisReference node, ClassScope scope) {
		setGeneratedBy(node, source);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(ThrowStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TrueLiteral node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TryStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeDeclaration node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeDeclaration node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeDeclaration node, CompilationUnitScope scope) {
		setGeneratedBy(node, source);
		applyOffset(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeParameter node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetVariable(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(TypeParameter node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetVariable(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(UnaryExpression node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(WhileStatement node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetASTNode(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Wildcard node, BlockScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
	
	@Override public boolean visit(Wildcard node, ClassScope scope) {
		setGeneratedBy(node, source);
		applyOffsetExpression(node);
		return super.visit(node, scope);
	}
}