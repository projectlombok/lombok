/*
 * Copyright (C) 2024 The Project Lombok Authors.
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
/* Copyright (C) 2021-2023 The Project Lombok Authors.
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

import lombok.ConfigurationKeys;
import lombok.core.AST;
import lombok.eclipse.EclipseNode;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

/**
 * Container for static utility methods used by the Locked[.Read/Write] annotations for eclipse.
 */
public final class HandleLockedUtil {
	private static final char[] INSTANCE_LOCK_NAME = "$lock".toCharArray();
	private static final char[] STATIC_LOCK_NAME = "$LOCK".toCharArray();
	private static final char[] LOCK_METHOD = "lock".toCharArray();
	private static final char[] UNLOCK_METHOD = "unlock".toCharArray();
	
	private HandleLockedUtil() {
		//Prevent instantiation
	}
	
	public static void preHandle(String annotationValue, char[][] lockTypeClass, char[][] lockImplClass, EclipseNode annotationNode) {
		EclipseNode methodNode = annotationNode.up();
		if (methodNode == null || methodNode.getKind() != AST.Kind.METHOD || !(methodNode.get() instanceof MethodDeclaration)) return;
		MethodDeclaration method = (MethodDeclaration) methodNode.get();
		if (method.isAbstract()) return;
		
		createLockField(annotationValue, annotationNode, lockTypeClass, lockImplClass, new AtomicBoolean(method.isStatic()), false);
	}
	
	private static char[] createLockField(String name, EclipseNode annotationNode, char[][] lockTypeClass, char[][] lockImplClass, AtomicBoolean isStatic, boolean reportErrors) {
		char[] lockName = name.toCharArray();
		
		Annotation source = (Annotation) annotationNode.get();
		if (lockName.length == 0) {
			lockName = isStatic.get() ? STATIC_LOCK_NAME : INSTANCE_LOCK_NAME;
		}
		
		EclipseNode typeNode = upToTypeNode(annotationNode);
		MemberExistsResult exists = MemberExistsResult.NOT_EXISTS;
		
		QualifiedTypeReference lockType = new QualifiedTypeReference(lockTypeClass, new long[] { 0, 0, 0, 0, 0 });
		
		if (typeNode != null && typeNode.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
			if (typeDecl.fields != null) for (FieldDeclaration def : typeDecl.fields) {
				char[] fName = def.name;
				if (fName == null) continue;
				if (Arrays.equals(fName, lockName)) {
					exists = getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					boolean st = def.isStatic();
					if (st != isStatic.get() && exists == MemberExistsResult.EXISTS_BY_LOMBOK) {
						if (reportErrors) annotationNode.addError(String.format("The generated field %s does not match the static status of this method", new String(lockName)));
						return null;
					}
					isStatic.set(st);
					
					if (exists == MemberExistsResult.EXISTS_BY_LOMBOK && !Arrays.deepEquals(lockType.getTypeName(), def.type.getTypeName())) {
						annotationNode.addError("Expected field " + new String(lockName) + " to be of type " + lockType +
							" but got type " + def.type + ". Did you mix @Locked with @Locked.Read/Write on the same generated field?");
						return null;
					}
					break;
				}
			}
		}
		
		if (exists == MemberExistsResult.NOT_EXISTS) {
			FieldDeclaration fieldDecl = setGeneratedBy(new FieldDeclaration(lockName, 0, -1), source);
			fieldDecl.declarationSourceEnd = -1;
			
			fieldDecl.modifiers = (isStatic.get() ? Modifier.STATIC : 0) | Modifier.FINAL | Modifier.PRIVATE;
			
			AllocationExpression lockAlloc = setGeneratedBy(new AllocationExpression(), source);
			lockAlloc.type = setGeneratedBy(new QualifiedTypeReference(lockImplClass, new long[] { 0, 0, 0, 0, 0 }), source);
			fieldDecl.type = setGeneratedBy(new QualifiedTypeReference(lockTypeClass, new long[] { 0, 0, 0, 0, 0 }), source);
			fieldDecl.initialization = lockAlloc;
			injectFieldAndMarkGenerated(annotationNode.up().up(), fieldDecl);
		}
		
		return lockName;
	}
	
	/**
	 * See {@link #handle(String, Annotation, EclipseNode, String, char[][], char[])} for
	 * {@code lockableMethodName = null}.
	 */
	public static void handle(String annotationValue, Annotation ast, EclipseNode annotationNode, String annotationName, char[][] lockTypeClass, char[][] lockImplClass) {
		handle(annotationValue, ast, annotationNode, annotationName, lockTypeClass, lockImplClass, null);
	}
	
	/**
	 * Called when an annotation is found that is likely to match the annotation you're interested in.
	 *
	 * Be aware that you'll be called for ANY annotation node in the source that looks like a match. There is,
	 * for example, no guarantee that the annotation node belongs to a method, even if you set your
	 * TargetType in the annotation to methods only.
	 *
	 * @param annotationValue The value of the annotation. This will be the name of the object used for locking.
	 * @param source The Eclipse AST node representing the annotation.
	 * @param annotationNode The Lombok AST wrapper around the 'ast' parameter. You can use this object
	 * to travel back up the chain (something javac AST can't do) to the parent of the annotation, as well
	 * as access useful methods such as generating warnings or errors focused on the annotation.
	 * @param annotationName The name of the annotation to use when referencing it in errors.
	 * @param lockTypeClass The fully qualified type of the variable when generating a lock to use.
	 * @param lockImplClass Call the constructor of this fully qualified classname to generate a lock to use.
	 * @param lockableMethodName The name of the method in the {@code lockClass} that returns a
	 * {@link java.util.concurrent.locks.Lock} object. When this is {@code null}, it is assumed that {@code lockClass}
	 * itself can be locked/unlocked.
	 */
	public static void handle(String annotationValue, Annotation source, EclipseNode annotationNode,
		String annotationName, char[][] lockTypeClass, char[][] lockImplClass, char[] lockableMethodName) {
		
		handleFlagUsage(annotationNode, ConfigurationKeys.LOCKED_FLAG_USAGE, annotationName);
		
		int p1 = source.sourceStart -1;
		int p2 = source.sourceStart -2;
		long pos = (((long) p1) << 32) | p2;
		
		EclipseNode methodNode = annotationNode.up();
		if (methodNode == null || methodNode.getKind() != AST.Kind.METHOD || !(methodNode.get() instanceof MethodDeclaration)) {
			annotationNode.addError(annotationName + " is legal only on methods.");
			return;
		}
		
		MethodDeclaration method = (MethodDeclaration) methodNode.get();
		if (method.isAbstract()) {
			annotationNode.addError(annotationName + " is legal only on concrete methods.");
			return;
		}
		
		EclipseNode typeNode = upToTypeNode(annotationNode);
		if (!isClassOrEnum(typeNode)) {
			annotationNode.addError(annotationName + " is legal only on methods in classes and enums.");
			return;
		}
		
		AtomicBoolean isStatic = new AtomicBoolean(method.isStatic());
		char[] lockName = createLockField(annotationValue, annotationNode, lockTypeClass, lockImplClass, isStatic, true);
		if (lockName == null) return;
		if (method.statements == null) return;
		
		Block block = new Block(0);
		block.statements = method.statements;
		setGeneratedBy(block, source);
		
		// Positions for in-method generated nodes are special
		block.sourceEnd = method.bodyEnd;
		block.sourceStart = method.bodyStart;
		
		Statement acquireLock = getLockingStatement(source, typeNode, LOCK_METHOD, lockName, lockableMethodName, isStatic.get(), p1, p2, pos);
		Statement unLock = getLockingStatement(source, typeNode, UNLOCK_METHOD, lockName, lockableMethodName, isStatic.get(), p1, p2, pos);
		
		TryStatement tryStatement = new TryStatement();
		tryStatement.tryBlock = block;
		tryStatement.finallyBlock = new Block(0);
		tryStatement.finallyBlock.statements = new Statement[] { unLock };
		
		method.statements = new Statement[] { acquireLock, tryStatement };
		
		// Positions for in-method generated nodes are special
		method.statements[0].sourceEnd = method.bodyEnd;
		method.statements[0].sourceStart = method.bodyStart;
		
		methodNode.rebuild();
	}
	
	private static Statement getLockingStatement(ASTNode source, EclipseNode typeNode, char[] lockMethod,
		char[] lockableObjectName, char[] lockableMethodName, boolean isStatic, int p1, int p2, long pos) {
		
		MessageSend lockStat = setGeneratedBy(new MessageSend(), source);
		lockStat.receiver = getLockable(source, typeNode, lockableObjectName, lockableMethodName, isStatic, p1, p2, pos);
		lockStat.selector = lockMethod;
		lockStat.nameSourcePosition = pos;
		lockStat.sourceStart = p1;
		lockStat.sourceEnd = lockStat.statementEnd = p2;
		return lockStat;
	}
	
	private static Expression getLockable(ASTNode source, EclipseNode typeNode, char[] lockName,
		char[] lockableMethodName, boolean isStatic, int p1, int p2, long pos) {
		
		Reference lockVariable;
		if (isStatic) {
			char[][] n = getQualifiedInnerName(typeNode, lockName);
			long[] ps = new long[n.length];
			Arrays.fill(ps, pos);
			lockVariable = new QualifiedNameReference(n, ps, p1, p2);
		} else {
			lockVariable = new FieldReference(lockName, pos);
			ThisReference thisReference = new ThisReference(p1, p2);
			setGeneratedBy(thisReference, source);
			((FieldReference) lockVariable).receiver = thisReference;
		}
		setGeneratedBy(lockVariable, source);
		
		Expression lockable;
		if (lockableMethodName == null) lockable = lockVariable;
		else {
			lockable = new MessageSend();
			((MessageSend) lockable).receiver = lockVariable;
			((MessageSend) lockable).selector = lockableMethodName;
			((MessageSend) lockable).nameSourcePosition = pos;
			lockable.sourceStart = p1;
			lockable.sourceEnd = lockable.statementEnd = p2;
		}
		return setGeneratedBy(lockable, source);
	}
}
