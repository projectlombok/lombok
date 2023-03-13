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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.Javac.*;
import static lombok.javac.JavacTreeMaker.TreeTag.treeTag;
import static lombok.javac.JavacTreeMaker.TypeTag.typeTag;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.NonNull;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

@Provides
@HandlerPriority(value = 512) // 2^9; onParameter=@__(@NonNull) has to run first.
public class HandleNonNull extends JavacAnnotationHandler<NonNull> {
	private JCMethodDecl createRecordArgslessConstructor(JavacNode typeNode, JavacNode source, JCMethodDecl existingCtr) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		java.util.List<JCVariableDecl> fields = new ArrayList<JCVariableDecl>();
		for (JavacNode child : typeNode.down()) {
			if (child.getKind() == Kind.FIELD) {
				JCVariableDecl v = (JCVariableDecl) child.get();
				if ((v.mods.flags & RECORD) != 0) {
					fields.add(v);
				}
			}
		}
		
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		
		for (int i = 0; i < fields.size(); i++) {
			JCVariableDecl arg = fields.get(i);
			JCModifiers mods = maker.Modifiers(GENERATED_MEMBER | Flags.PARAMETER, arg.mods.annotations);
			params.append(maker.VarDef(mods, arg.name, arg.vartype, null));
		}
		
		JCModifiers mods = maker.Modifiers(toJavacModifier(AccessLevel.PUBLIC) | COMPACT_RECORD_CONSTRUCTOR, List.<JCAnnotation>nil());
		JCBlock body = maker.Block(0L, List.<JCStatement>nil());
		if (existingCtr == null) {
			JCMethodDecl constr = maker.MethodDef(mods, typeNode.toName("<init>"), null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(), body, null);
			return recursiveSetGeneratedBy(constr, source);
		} else {
			existingCtr.mods = mods;
			existingCtr.body = body;
			existingCtr = recursiveSetGeneratedBy(existingCtr, source);
			addSuppressWarningsAll(existingCtr.mods, typeNode, typeNode.getNodeFor(getGeneratedBy(existingCtr)), typeNode.getContext());
			addGenerated(existingCtr.mods, typeNode, typeNode.getNodeFor(getGeneratedBy(existingCtr)), typeNode.getContext());
			return existingCtr;
		}
	}
	
	/**
	 * If the provided typeNode is a record, returns the compact constructor (there should only be one, but if the file is
	 * not semantically sound there might be more). If the only one in existence is the default auto-generated one, it is removed,
	 * a new explicit one is created, and that one is returned in a list.
	 * 
	 * Otherwise, an empty list is returned.
	 */
	private List<JCMethodDecl> addCompactConstructorIfNeeded(JavacNode typeNode, JavacNode source) {
		List<JCMethodDecl> answer = List.nil();
		
		if (typeNode == null || !(typeNode.get() instanceof JCClassDecl)) return answer;
		
		JCClassDecl cDecl = (JCClassDecl) typeNode.get();
		if ((cDecl.mods.flags & RECORD) == 0) return answer;
		
		boolean generateConstructor = false;
		
		JCMethodDecl existingCtr = null;
		
		for (JCTree def : cDecl.defs) {
			if (def instanceof JCMethodDecl) {
				JCMethodDecl md = (JCMethodDecl) def;
				if (md.name.contentEquals("<init>")) {
					if ((md.mods.flags & Flags.GENERATEDCONSTR) != 0) {
						existingCtr = md;
						existingCtr.mods.flags = existingCtr.mods.flags & ~Flags.GENERATEDCONSTR;
						generateConstructor = true;
					} else {
						if (!isTolerate(typeNode, md)) {
							if ((md.mods.flags & COMPACT_RECORD_CONSTRUCTOR) != 0) {
								generateConstructor = false;
								answer = answer.prepend(md);
							}
						}
					}
				}
			}
		}
		
		if (generateConstructor) {
			JCMethodDecl ctr;
			if (existingCtr != null) {
				ctr = createRecordArgslessConstructor(typeNode, source, existingCtr);
			} else {
				ctr = createRecordArgslessConstructor(typeNode, source, null);
				injectMethod(typeNode, ctr);
			}
			answer = answer.prepend(ctr);
		}
		
		return answer;
	}
	
	private void addNullCheckIfNeeded(JCMethodDecl method, JavacNode paramNode, JavacNode source) {
		// Possibly, if 'declaration instanceof ConstructorDeclaration', fetch declaration.constructorCall, search it for any references to our parameter,
		// and if they exist, create a new method in the class: 'private static <T> T lombok$nullCheck(T expr, String msg) {if (expr == null) throw NPE; return expr;}' and
		// wrap all references to it in the super/this to a call to this method.
		
		JCStatement nullCheck = recursiveSetGeneratedBy(generateNullCheck(source.getTreeMaker(), paramNode, source), source);
		
		if (nullCheck == null) {
			// @NonNull applied to a primitive. Kinda pointless. Let's generate a warning.
			source.addWarning("@NonNull is meaningless on a primitive.");
			return;
		}
		
		List<JCStatement> statements = method.body.stats;
		
		String expectedName = paramNode.getName();
		
		/* Abort if the null check is already there, delving into try and synchronized statements */ {
			List<JCStatement> stats = statements;
			int idx = 0;
			while (stats.size() > idx) {
				JCStatement stat = stats.get(idx++);
				if (JavacHandlerUtil.isConstructorCall(stat)) continue;
				if (stat instanceof JCTry) {
					stats = ((JCTry) stat).body.stats;
					idx = 0;
					continue;
				}
				if (stat instanceof JCSynchronized) {
					stats = ((JCSynchronized) stat).body.stats;
					idx = 0;
					continue;
				}
				String varNameOfNullCheck = returnVarNameIfNullCheck(stat);
				if (varNameOfNullCheck == null) break;
				if (varNameOfNullCheck.equals(expectedName)) return;
			}
		}
		
		List<JCStatement> tail = statements;
		List<JCStatement> head = List.nil();
		for (JCStatement stat : statements) {
			if (JavacHandlerUtil.isConstructorCall(stat) || (JavacHandlerUtil.isGenerated(stat) && isNullCheck(stat))) {
				tail = tail.tail;
				head = head.prepend(stat);
				continue;
			}
			break;
		}
		
		List<JCStatement> newList = tail.prepend(nullCheck);
		for (JCStatement stat : head) newList = newList.prepend(stat);
		method.body.stats = newList;
		source.getAst().setChanged();
	}
	
	@Override public void handle(AnnotationValues<NonNull> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.NON_NULL_FLAG_USAGE, "@NonNull");
		
		final JavacNode node;
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
			
			try {
				if (isPrimitive(((JCVariableDecl) node.get()).vartype)) {
					annotationNode.addWarning("@NonNull is meaningless on a primitive.");
				}
			} catch (Exception ignore) {}
			
			JCVariableDecl fDecl = (JCVariableDecl) node.get();
			if ((fDecl.mods.flags & RECORD) != 0) {
				// well, these kinda double as parameters (of the compact constructor), so we do some work here.
				
				List<JCMethodDecl> compactConstructors = addCompactConstructorIfNeeded(node.up(), annotationNode);
				for (JCMethodDecl ctr : compactConstructors) {
					addNullCheckIfNeeded(ctr, node, annotationNode);
				}
			}
			return;
		}
		
		if (node.getKind() != Kind.ARGUMENT) return;
		
		JCMethodDecl declaration;
		try {
			declaration = (JCMethodDecl) node.up().get();
		} catch (Exception e) {
			return;
		}
		
		if (declaration.body == null) {
			// This used to be a warning, but as @NonNull also has a documentary purpose, better to not warn about this. Since 1.16.7
			return;
		}
		
		if ((declaration.mods.flags & (GENERATED_MEMBER | COMPACT_RECORD_CONSTRUCTOR)) != 0) {
			// The 'real' annotations are on the `record Foo(@NonNull Obj x)` part and we just see these
			// syntax-sugared over. We deal with it on the field declaration variant, as those are always there,
			// not dependent on whether you write out the compact constructor or not.
			return;
		}
		
		addNullCheckIfNeeded(declaration, node, annotationNode);
	}
	
	public boolean isNullCheck(JCStatement stat) {
		return returnVarNameIfNullCheck(stat) != null;
	}
	
	/**
	 * Checks if the statement is of the form 'if (x == null) {throw WHATEVER;}' or 'assert x != null;',
	 * where the block braces are optional. If it is of this form, returns "x".
	 * If it is not of this form, returns null.
	 */
	public String returnVarNameIfNullCheck(JCStatement stat) {
		boolean isIf = stat instanceof JCIf;
		boolean isExpression = stat instanceof JCExpressionStatement;
		if (!isIf && !(stat instanceof JCAssert) && !isExpression) return null;
		
		if (isExpression) {
			/* Check if the statements contains a call to checkNotNull or requireNonNull */
			JCExpression expression = ((JCExpressionStatement) stat).expr;
			if (expression instanceof JCAssign) expression = ((JCAssign) expression).rhs;
			if (!(expression instanceof JCMethodInvocation)) return null;
			
			JCMethodInvocation invocation = (JCMethodInvocation) expression;
			JCExpression method = invocation.meth;
			Name name = null;
			if (method instanceof JCFieldAccess) {
				name = ((JCFieldAccess) method).name;
			} else if (method instanceof JCIdent) {
				name = ((JCIdent) method).name;
			}
			if (name == null || (!name.contentEquals("checkNotNull") && !name.contentEquals("requireNonNull"))) return null;
			
			if (invocation.args.isEmpty()) return null;
			JCExpression firstArgument = invocation.args.head;
			if (!(firstArgument instanceof JCIdent)) return null;
			return ((JCIdent) firstArgument).toString();
		}
		
		if (isIf) {
			/* Check that the if's statement is a throw statement, possibly in a block. */
			JCStatement then = ((JCIf) stat).thenpart;
			if (then instanceof JCBlock) {
				List<JCStatement> stats = ((JCBlock) then).stats;
				if (stats.length() == 0) return null;
				then = stats.get(0);
			}
			if (!(then instanceof JCThrow)) return null;
		}
		
		/* Check that the if's conditional is like 'x == null'. Return from this method (don't generate
		   a nullcheck) if 'x' is equal to our own variable's name: There's already a nullcheck here. */ {
			JCExpression cond = isIf ? ((JCIf) stat).cond : ((JCAssert) stat).cond;
			while (cond instanceof JCParens) cond = ((JCParens) cond).expr;
			if (!(cond instanceof JCBinary)) return null;
			JCBinary bin = (JCBinary) cond;
			if (isIf) {
				if (!CTC_EQUAL.equals(treeTag(bin))) return null;
			} else {
				if (!CTC_NOT_EQUAL.equals(treeTag(bin))) return null;
			}
			if (!(bin.lhs instanceof JCIdent)) return null;
			if (!(bin.rhs instanceof JCLiteral)) return null;
			if (!CTC_BOT.equals(typeTag(bin.rhs))) return null;
			return ((JCIdent) bin.lhs).name.toString();
		}
	}
}
