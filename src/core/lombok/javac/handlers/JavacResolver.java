/*
 * Copyright (C) 2012 The Project Lombok Authors.
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

import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public enum JavacResolver {
	CLASS {
		@Override
		public Type resolveMember(JavacNode node, JCExpression expr) {
			Type type = expr.type;
			if (type == null) {
				try {
					new JavacResolution(node.getContext()).resolveClassMember(node);
					type = expr.type;
				} catch (Exception ignore) {
				}
			}
			return type;
		}
	},
	METHOD {
		public Type resolveMember(JavacNode node, JCExpression expr) {
			Type type = expr.type;
			if (type == null) {
				try {
					JCExpression resolvedExpression = ((JCExpression) new JavacResolution(node.getContext()).resolveMethodMember(node).get(expr));
					if (resolvedExpression != null) type = resolvedExpression.type;
				} catch (Exception ignore) {
				}
			}
			return type;
		}
	},
	CLASS_AND_METHOD {
		@Override
		public Type resolveMember(JavacNode node, JCExpression expr) {
			Type type = METHOD.resolveMember(node, expr);
			if (type == null) {
				JavacNode classNode = node;
				while (classNode != null && noneOf(classNode.get(), JCBlock.class, JCMethodDecl.class, JCVariableDecl.class)) {
					classNode = classNode.up();
				}
				if (classNode != null) {
					type = CLASS.resolveMember(classNode, expr);
				}
			}
			return type;
		}
		
		private boolean noneOf(Object o, Class<?>... clazzes) {
			for (Class<?> clazz : clazzes) {
				if (clazz.isInstance(o)) return false;
			}
			return true;
		}
	};
	
	

	public abstract Type resolveMember(final JavacNode node, final JCExpression expr);
}
