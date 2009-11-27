/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

import lombok.Synchronized;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

/**
 * Handles the {@code lombok.Synchronized} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSynchronized implements JavacAnnotationHandler<Synchronized> {
	private static final String INSTANCE_LOCK_NAME = "$lock";
	private static final String STATIC_LOCK_NAME = "$LOCK";
	
	@Override public boolean handle(AnnotationValues<Synchronized> annotation, JCAnnotation ast, JavacNode annotationNode) {
		markAnnotationAsProcessed(annotationNode, Synchronized.class);
		JavacNode methodNode = annotationNode.up();
		
		if (methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof JCMethodDecl)) {
			annotationNode.addError("@Synchronized is legal only on methods.");
			
			return true;
		}
		
		JCMethodDecl method = (JCMethodDecl)methodNode.get();
		
		if ((method.mods.flags & Flags.ABSTRACT) != 0) {
			annotationNode.addError("@Synchronized is legal only on concrete methods.");
			
			return true;
		}
		boolean isStatic = (method.mods.flags & Flags.STATIC) != 0;
		String lockName = annotation.getInstance().value();
		boolean autoMake = false;
		if (lockName.length() == 0) {
			autoMake = true;
			lockName = isStatic ? STATIC_LOCK_NAME : INSTANCE_LOCK_NAME;
		}
		
		TreeMaker maker = methodNode.getTreeMaker();
		
		if (fieldExists(lockName, methodNode) == MemberExistsResult.NOT_EXISTS) {
			if (!autoMake) {
				annotationNode.addError("The field " + lockName + " does not exist.");
				return true;
			}
			JCExpression objectType = chainDots(maker, methodNode, "java", "lang", "Object");
			//We use 'new Object[0];' because quite unlike 'new Object();', empty arrays *ARE* serializable!
			JCNewArray newObjectArray = maker.NewArray(chainDots(maker, methodNode, "java", "lang", "Object"),
					List.<JCExpression>of(maker.Literal(TypeTags.INT, 0)), null);
			JCVariableDecl fieldDecl = maker.VarDef(
					maker.Modifiers(Flags.FINAL | (isStatic ? Flags.STATIC : 0)),
					methodNode.toName(lockName), objectType, newObjectArray);
			injectField(methodNode.up(), fieldDecl);
		}
		
		if (method.body == null) return false;
		
		JCExpression lockNode = maker.Ident(methodNode.toName(lockName));
		
		method.body = maker.Block(0, List.<JCStatement>of(maker.Synchronized(lockNode, method.body)));
		
		methodNode.rebuild();
		
		return true;
	}
}
