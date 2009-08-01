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
package lombok.eclipse.handlers;

import static lombok.eclipse.Eclipse.fromQualifiedName;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.core.AST.Kind;
import lombok.eclipse.EclipseAST;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

class PKG {
	private PKG() {}
	
	static int toModifier(AccessLevel value) {
		switch ( value ) {
		case MODULE:
		case PACKAGE:
			return 0;
		default:
		case PUBLIC:
			return Modifier.PUBLIC;
		case PROTECTED:
			return Modifier.PROTECTED;
		case PRIVATE:
			return Modifier.PRIVATE;
		}
	}
	
	static boolean nameEquals(char[][] typeName, String string) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for ( char[] elem : typeName ) {
			if ( first ) first = false;
			else sb.append('.');
			sb.append(elem);
		}
		
		return string.contentEquals(sb);
	}
	
	enum MemberExistsResult {
		NOT_EXISTS, EXISTS_BY_USER, EXISTS_BY_LOMBOK;
	}
	
	static MemberExistsResult fieldExists(String fieldName, EclipseAST.Node node) {
		while ( node != null && !(node.get() instanceof TypeDeclaration) ) {
			node = node.up();
		}
		
		if ( node != null && node.get() instanceof TypeDeclaration ) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if ( typeDecl.fields != null ) for ( FieldDeclaration def : typeDecl.fields ) {
				char[] fName = def.name;
				if ( fName == null ) continue;
				if ( fieldName.equals(new String(fName)) ) {
					EclipseAST.Node existing = node.getNodeFor(def);
					if ( existing == null || !existing.isHandled() ) return MemberExistsResult.EXISTS_BY_USER;
					return MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	static MemberExistsResult methodExists(String methodName, EclipseAST.Node node) {
		while ( node != null && !(node.get() instanceof TypeDeclaration) ) {
			node = node.up();
		}
		
		if ( node != null && node.get() instanceof TypeDeclaration ) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if ( typeDecl.methods != null ) for ( AbstractMethodDeclaration def : typeDecl.methods ) {
				char[] mName = def.selector;
				if ( mName == null ) continue;
				if ( methodName.equals(new String(mName)) ) {
					EclipseAST.Node existing = node.getNodeFor(def);
					if ( existing == null || !existing.isHandled() ) return MemberExistsResult.EXISTS_BY_USER;
					return MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	static MemberExistsResult constructorExists(EclipseAST.Node node) {
		while ( node != null && !(node.get() instanceof TypeDeclaration) ) {
			node = node.up();
		}
		
		if ( node != null && node.get() instanceof TypeDeclaration ) {
			TypeDeclaration typeDecl = (TypeDeclaration)node.get();
			if ( typeDecl.methods != null ) for ( AbstractMethodDeclaration def : typeDecl.methods ) {
				if ( def instanceof ConstructorDeclaration ) {
					if ( (def.bits & ASTNode.IsDefaultConstructor) != 0 ) continue;
					EclipseAST.Node existing = node.getNodeFor(def);
					if ( existing == null || !existing.isHandled() ) return MemberExistsResult.EXISTS_BY_USER;
					return MemberExistsResult.EXISTS_BY_LOMBOK;
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	static EclipseAST.Node getExistingLombokConstructor(EclipseAST.Node node) {
		while ( node != null && !(node.get() instanceof TypeDeclaration) ) {
			node = node.up();
		}
		
		if ( node.get() instanceof TypeDeclaration ) {
			for ( AbstractMethodDeclaration def : ((TypeDeclaration)node.get()).methods ) {
				if ( def instanceof ConstructorDeclaration ) {
					if ( (def.bits & ASTNode.IsDefaultConstructor) != 0 ) continue;
					EclipseAST.Node existing = node.getNodeFor(def);
					if ( existing.isHandled() ) return existing;
				}
			}
		}
		
		return null;
	}
	
	static EclipseAST.Node getExistingLombokMethod(String methodName, EclipseAST.Node node) {
		while ( node != null && !(node.get() instanceof TypeDeclaration) ) {
			node = node.up();
		}
		
		if ( node.get() instanceof TypeDeclaration ) {
			for ( AbstractMethodDeclaration def : ((TypeDeclaration)node.get()).methods ) {
				char[] mName = def.selector;
				if ( mName == null ) continue;
				if ( methodName.equals(new String(mName)) ) {
					EclipseAST.Node existing = node.getNodeFor(def);
					if ( existing.isHandled() ) return existing;
				}
			}
		}
		
		return null;
	}
	
	static void injectField(EclipseAST.Node type, FieldDeclaration field) {
		TypeDeclaration parent = (TypeDeclaration) type.get();
		
		if ( parent.fields == null ) {
			parent.fields = new FieldDeclaration[1];
			parent.fields[0] = field;
		} else {
			FieldDeclaration[] newArray = new FieldDeclaration[parent.fields.length + 1];
			System.arraycopy(parent.fields, 0, newArray, 0, parent.fields.length);
			newArray[parent.fields.length] = field;
			parent.fields = newArray;
		}
		
		type.add(field, Kind.FIELD).recursiveSetHandled();
	}
	
	static void injectMethod(EclipseAST.Node type, AbstractMethodDeclaration method) {
		TypeDeclaration parent = (TypeDeclaration) type.get();
		
		if ( parent.methods == null ) {
			parent.methods = new AbstractMethodDeclaration[1];
			parent.methods[0] = method;
		} else {
			boolean injectionComplete = false;
			if ( method instanceof ConstructorDeclaration ) {
				for ( int i = 0 ; i < parent.methods.length ; i++ ) {
					if ( parent.methods[i] instanceof ConstructorDeclaration &&
							(parent.methods[i].bits & ASTNode.IsDefaultConstructor) != 0 ) {
						EclipseAST.Node tossMe = type.getNodeFor(parent.methods[i]);
						parent.methods[i] = method;
						if ( tossMe != null ) tossMe.up().removeChild(tossMe);
						injectionComplete = true;
						break;
					}
				}
			}
			if ( !injectionComplete ) {
				AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[parent.methods.length + 1];
				System.arraycopy(parent.methods, 0, newArray, 0, parent.methods.length);
				newArray[parent.methods.length] = method;
				parent.methods = newArray;
			}
		}
		
		type.add(method, Kind.METHOD).recursiveSetHandled();
	}
	
	static Annotation[] findNonNullAnnotations(FieldDeclaration field) {
		List<Annotation> result = new ArrayList<Annotation>();
		for (Annotation annotation : field.annotations) {
			TypeReference typeRef = annotation.type;
			if ( typeRef != null && typeRef.getTypeName()!= null ) {
				char[][] typeName = typeRef.getTypeName();
				String suspect = new String(typeName[typeName.length - 1]);
				if (suspect.equalsIgnoreCase("NonNull") || suspect.equalsIgnoreCase("NotNull")) {
					result.add(annotation);
				}
			}
		}	
		return result.toArray(new Annotation[0]);
	}
	

	static Statement generateNullCheck(AbstractVariableDeclaration variable) {
		AllocationExpression exception = new AllocationExpression();
		exception.type = new QualifiedTypeReference(fromQualifiedName("java.lang.NullPointerException"), new long[]{0, 0, 0});
		exception.arguments = new Expression[] { new StringLiteral(variable.name, 0, variable.name.length - 1, 0)};
		ThrowStatement throwStatement = new ThrowStatement(exception, 0, 0);
		
		return new IfStatement(new EqualExpression(new SingleNameReference(variable.name, 0),
				new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL), throwStatement, 0, 0);
	}
}
