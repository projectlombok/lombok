/*
 * Copyright (C) 2013-2021 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import lombok.ConfigurationKeys;
import lombok.NonNull;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.EcjAugments;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.spi.Provides;

@Provides
@HandlerPriority(value = 512) // 2^9; onParameter=@__(@NonNull) has to run first.
public class HandleNonNull extends EclipseAnnotationHandler<NonNull> {
	private static final char[] REQUIRE_NON_NULL = "requireNonNull".toCharArray();
	private static final char[] CHECK_NOT_NULL = "checkNotNull".toCharArray();
	
	public static final HandleNonNull INSTANCE = new HandleNonNull();
	
	public void fix(EclipseNode method) {
		for (EclipseNode m : method.down()) {
			if (m.getKind() != Kind.ARGUMENT) continue;
			for (EclipseNode c : m.down()) {
				if (c.getKind() == Kind.ANNOTATION) {
					if (annotationTypeMatches(NonNull.class, c)) {
						handle0((Annotation) c.get(), c, true);
					}
				}
			}
		}
	}
	
	private List<FieldDeclaration> getRecordComponents(EclipseNode typeNode) {
		List<FieldDeclaration> list = new ArrayList<FieldDeclaration>();
		
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() == Kind.FIELD) {
				FieldDeclaration fd = (FieldDeclaration) child.get();
				if ((fd.modifiers & AccRecord) != 0) list.add(fd);
			}
		}
		
		return list;
	}
	
	private EclipseNode addCompactConstructorIfNeeded(EclipseNode typeNode, EclipseNode annotationNode) {
		// explicit Compact Constructor has bits set: Bit32, IsCanonicalConstructor (10).
		// implicit Compact Constructor has bits set: Bit32, IsCanonicalConstructor (10), and IsImplicit (11).
		// explicit constructor with long-form shows up as a normal constructor (Bit32 set, that's all), but the
		//   implicit CC is then also present and will presumably be stripped out in some later phase.
		
		EclipseNode toRemove = null;
		EclipseNode existingCompactConstructor = null;
		List<FieldDeclaration> recordComponents = null;
		for (EclipseNode child : typeNode.down()) {
			if (!(child.get() instanceof ConstructorDeclaration)) continue;
			ConstructorDeclaration cd = (ConstructorDeclaration) child.get();
			if ((cd.bits & IsCanonicalConstructor) != 0) {
				if ((cd.bits & IsImplicit) != 0) {
					toRemove = child;
				} else {
					existingCompactConstructor = child;
				}
			} else {
				// If this constructor has exact matching types vs. the record components,
				// this is the canonical constructor in long form and we should not generate one.
				
				if (recordComponents == null) recordComponents = getRecordComponents(typeNode);
				int argLength = cd.arguments == null ? 0 : cd.arguments.length;
				int compLength = recordComponents.size();
				boolean isCanonical = argLength == compLength;
				if (isCanonical) top: for (int i = 0; i < argLength; i++) {
					TypeReference a = recordComponents.get(i).type;
					TypeReference b = cd.arguments[i] == null ? null : cd.arguments[i].type;
					// technically this won't match e.g. `java.lang.String` to just `String`;
					// to use this feature you'll need to use the same way to write it, which seems
					// like a fair requirement.
					char[][] ta = getRawTypeName(a);
					char[][] tb = getRawTypeName(b);
					if (ta == null || tb == null || ta.length != tb.length) {
						isCanonical = false;
						break top;
					}
					for (int j = 0; j < ta.length; j++) {
						if (!Arrays.equals(ta[j], tb[j])) {
							isCanonical = false;
							break top;
						}
					}
				}
				if (isCanonical) {
					return null;
				}
			}
		}
		if (existingCompactConstructor != null) return existingCompactConstructor;
		int posToInsert = -1;
		TypeDeclaration td = (TypeDeclaration) typeNode.get();
		if (toRemove != null) {
			int idxToRemove = -1;
			for (int i = 0; i < td.methods.length; i++) {
				if (td.methods[i] == toRemove.get()) idxToRemove = i;
			}
			if (idxToRemove != -1) {
				System.arraycopy(td.methods, idxToRemove + 1, td.methods, idxToRemove, td.methods.length - idxToRemove - 1);
				posToInsert = td.methods.length - 1;
				typeNode.removeChild(toRemove);
			}
		}
		if (posToInsert == -1) {
			AbstractMethodDeclaration[] na = new AbstractMethodDeclaration[td.methods.length + 1];
			posToInsert = td.methods.length;
			System.arraycopy(td.methods, 0, na, 0, posToInsert);
			td.methods = na;
		}
		
		ConstructorDeclaration cd = new ConstructorDeclaration(((CompilationUnitDeclaration) typeNode.top().get()).compilationResult);
		cd.modifiers = ClassFileConstants.AccPublic;
		cd.bits = ASTNode.Bit32 | ECLIPSE_DO_NOT_TOUCH_FLAG | IsCanonicalConstructor;
		cd.selector = td.name;
		cd.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		if (recordComponents == null) recordComponents = getRecordComponents(typeNode);
		cd.arguments = new Argument[recordComponents.size()];
		cd.statements = new Statement[recordComponents.size()];
		cd.bits = IsCanonicalConstructor;
		
		for (int i = 0; i < cd.arguments.length; i++) {
			FieldDeclaration cmp = recordComponents.get(i);
			cd.arguments[i] = new Argument(cmp.name, cmp.sourceStart, cmp.type, 0);
			cd.arguments[i].bits = ASTNode.IsArgument | ASTNode.IgnoreRawTypeCheck | ASTNode.IsReachable;
			FieldReference lhs = new FieldReference(cmp.name, 0);
			lhs.receiver = new ThisReference(0, 0);
			SingleNameReference rhs = new SingleNameReference(cmp.name, 0);
			cd.statements[i] = new Assignment(lhs, rhs, cmp.sourceEnd);
		}
		
		setGeneratedBy(cd, annotationNode.get());
		for (int i = 0; i < cd.arguments.length; i++) {
			FieldDeclaration cmp = recordComponents.get(i);
			cd.arguments[i].sourceStart = cmp.sourceStart;
			cd.arguments[i].sourceEnd = cmp.sourceStart;
			cd.arguments[i].declarationSourceEnd = cmp.sourceStart;
			cd.arguments[i].declarationEnd = cmp.sourceStart;
		}
		
		td.methods[posToInsert] = cd;
		cd.annotations = addSuppressWarningsAll(typeNode, cd, cd.annotations);
		cd.annotations = addGenerated(typeNode, cd, cd.annotations);
		return typeNode.add(cd, Kind.METHOD);
	}
	
	private static char[][] getRawTypeName(TypeReference a) {
		if (a instanceof QualifiedTypeReference) return ((QualifiedTypeReference) a).tokens;
		if (a instanceof SingleTypeReference) return new char[][] {((SingleTypeReference) a).token};
		return null;
	}
	
	@Override public void handle(AnnotationValues<NonNull> annotation, Annotation ast, EclipseNode annotationNode) {
		// Generating new methods is only possible during diet parse but modifying existing methods requires a full parse.
		// As we need both for @NonNull we reset the handled flag during diet parse.
		
		if (!annotationNode.isCompleteParse()) {
			final EclipseNode node;
			if (annotationNode.up().getKind() == Kind.TYPE_USE) {
				node = annotationNode.directUp().directUp();
			} else {
				node = annotationNode.up();
			}
			
			if (node.getKind() == Kind.FIELD) {
				//Check if this is a record and we need to generate the compact form constructor.
				EclipseNode typeNode = node.up();
				if (typeNode.getKind() == Kind.TYPE) {
					if (isRecord(typeNode)) {
						addCompactConstructorIfNeeded(typeNode, annotationNode);
					}
				}
			}
			
			EcjAugments.ASTNode_handled.clear(ast);
			return;
		}
		
		handle0(ast, annotationNode, false);
	}
	
	private EclipseNode findCompactConstructor(EclipseNode typeNode) {
		for (EclipseNode child : typeNode.down()) {
			if (!(child.get() instanceof ConstructorDeclaration)) continue;
			ConstructorDeclaration cd = (ConstructorDeclaration) child.get();
			if ((cd.bits & IsCanonicalConstructor) != 0 && (cd.bits & IsImplicit) == 0) return child;
		}
		
		return null;
	}
	
	private void handle0(Annotation ast, EclipseNode annotationNode, boolean force) {
		handleFlagUsage(annotationNode, ConfigurationKeys.NON_NULL_FLAG_USAGE, "@NonNull");
		
		final EclipseNode node;
		if (annotationNode.up().getKind() == Kind.TYPE_USE) {
			node = annotationNode.directUp().directUp();
		} else {
			node = annotationNode.up();
		}
		
		if (node.getKind() == Kind.FIELD) {
			// This is meaningless unless the field is used to generate a method (@Setter, @RequiredArgsConstructor, etc),
			// but in that case those handlers will take care of it. However, we DO check if the annotation is applied to
			// a primitive, because those handlers trigger on any annotation named @NonNull and we only want the warning
			// behaviour on _OUR_ 'lombok.NonNull'.
			EclipseNode fieldNode = node;
			EclipseNode typeNode = fieldNode.up();
			
			try {
				if (isPrimitive(((AbstractVariableDeclaration) node.get()).type)) {
					annotationNode.addWarning("@NonNull is meaningless on a primitive.");
					return;
				}
			} catch (Exception ignore) {}
			
			if (isRecord(typeNode)) {
				// well, these kinda double as parameters (of the compact constructor), so we do some work here.
				// NB:Tthe diet parse run already added an explicit compact constructor if we need to take any actions.
				EclipseNode compactConstructor = findCompactConstructor(typeNode);
				
				if (compactConstructor != null) {
					addNullCheckIfNeeded((AbstractMethodDeclaration) compactConstructor.get(), (AbstractVariableDeclaration) fieldNode.get(), annotationNode);
				}
			}
			
			return;
		}
		
		if (node.getKind() != Kind.ARGUMENT) return;
		
		Argument param;
		AbstractMethodDeclaration declaration;
		
		try {
			param = (Argument) node.get();
			declaration = (AbstractMethodDeclaration) node.up().get();
		} catch (Exception e) {
			return;
		}
		
		if (!force && isGenerated(declaration)) return;
		
		if (declaration.isAbstract()) {
			// This used to be a warning, but as @NonNull also has a documentary purpose, better to not warn about this. Since 1.16.7
			return;
		}
		
		addNullCheckIfNeeded(declaration, param, annotationNode);
		node.up().rebuild();
	}
	
	private void addNullCheckIfNeeded(AbstractMethodDeclaration declaration, AbstractVariableDeclaration param, EclipseNode annotationNode) {
		// Possibly, if 'declaration instanceof ConstructorDeclaration', fetch declaration.constructorCall, search it for any references to our parameter,
		// and if they exist, create a new method in the class: 'private static <T> T lombok$nullCheck(T expr, String msg) {if (expr == null) throw NPE; return expr;}' and
		// wrap all references to it in the super/this to a call to this method.
		
		Statement nullCheck = generateNullCheck(param, annotationNode, null);
		
		if (nullCheck == null) {
			// @NonNull applied to a primitive. Kinda pointless. Let's generate a warning.
			annotationNode.addWarning("@NonNull is meaningless on a primitive.");
			return;
		}
		
		if (declaration.statements == null) {
			declaration.statements = new Statement[] {nullCheck};
		} else {
			char[] expectedName = param.name;
			/* Abort if the null check is already there, delving into try and synchronized statements */ {
				Statement[] stats = declaration.statements;
				int idx = 0;
				while (stats != null && stats.length > idx) {
					Statement stat = stats[idx++];
					if (stat instanceof TryStatement) {
						stats = ((TryStatement) stat).tryBlock.statements;
						idx = 0;
						continue;
					}
					if (stat instanceof SynchronizedStatement) {
						stats = ((SynchronizedStatement) stat).block.statements;
						idx = 0;
						continue;
					}
					char[] varNameOfNullCheck = returnVarNameIfNullCheck(stat);
					if (varNameOfNullCheck == null) break;
					if (Arrays.equals(varNameOfNullCheck, expectedName)) return;
				}
			}
			
			Statement[] newStatements = new Statement[declaration.statements.length + 1];
			int skipOver = 0;
			for (Statement stat : declaration.statements) {
				if (isGenerated(stat) && isNullCheck(stat)) skipOver++;
				else break;
			}
			System.arraycopy(declaration.statements, 0, newStatements, 0, skipOver);
			System.arraycopy(declaration.statements, skipOver, newStatements, skipOver + 1, declaration.statements.length - skipOver);
			newStatements[skipOver] = nullCheck;
			declaration.statements = newStatements;
		}
	}
	
	public boolean isNullCheck(Statement stat) {
		return returnVarNameIfNullCheck(stat) != null;
	}
	
	public char[] returnVarNameIfNullCheck(Statement stat) {
		boolean isIf = stat instanceof IfStatement;
		boolean isExpression = stat instanceof Expression;
		if (!isIf && !(stat instanceof AssertStatement) && !isExpression) return null;
		
		if (isExpression) {
			/* Check if the statements contains a call to checkNotNull or requireNonNull */
			Expression expression = (Expression) stat;
			if (expression instanceof Assignment) expression = ((Assignment) expression).expression;
			if (!(expression instanceof MessageSend)) return null;
			
			MessageSend invocation = (MessageSend) expression;
			if (!Arrays.equals(invocation.selector, CHECK_NOT_NULL) && !Arrays.equals(invocation.selector, REQUIRE_NON_NULL)) return null;
			if (invocation.arguments == null || invocation.arguments.length == 0) return null;
			Expression firstArgument = invocation.arguments[0];
			if (!(firstArgument instanceof SingleNameReference)) return null;
			return ((SingleNameReference) firstArgument).token;
		}
		
		if (isIf) {
			/* Check that the if's statement is a throw statement, possibly in a block. */
			Statement then = ((IfStatement) stat).thenStatement;
			if (then instanceof Block) {
				Statement[] blockStatements = ((Block) then).statements;
				if (blockStatements == null || blockStatements.length == 0) return null;
				then = blockStatements[0];
			}
			
			if (!(then instanceof ThrowStatement)) return null;
		}
		
		/* Check that the if's conditional is like 'x == null'. Return from this method (don't generate
		   a nullcheck) if 'x' is equal to our own variable's name: There's already a nullcheck here. */ {
			Expression cond = isIf ? ((IfStatement) stat).condition : ((AssertStatement) stat).assertExpression;
			if (!(cond instanceof EqualExpression)) return null;
			EqualExpression bin = (EqualExpression) cond;
			String op = bin.operatorToString();
			if (isIf) {
				if (!"==".equals(op)) return null;
			} else {
				if (!"!=".equals(op)) return null;
			}
			if (!(bin.left instanceof SingleNameReference)) return null;
			if (!(bin.right instanceof NullLiteral)) return null;
			return ((SingleNameReference) bin.left).token;
		}
	}
}
