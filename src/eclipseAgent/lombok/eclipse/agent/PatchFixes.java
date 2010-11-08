/*
 * Copyright © 2010 Reinier Zwitserloot and Roel Spilker.
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

package lombok.eclipse.agent;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompiler;
import lombok.eclipse.Eclipse;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class PatchFixes {
	public static int fixRetrieveStartingCatchPosition(int in) {
		return in;
	}
	
	public static final int ALREADY_PROCESSED_FLAG = 0x800000;	//Bit 24
	
	public static boolean checkBit24(Object node) throws Exception {
		int bits = (Integer)(node.getClass().getField("bits").get(node));
		return (bits & ALREADY_PROCESSED_FLAG) != 0;
	}
	
	/**
	 * XXX LIVE DEBUG
	 * 
	 * Once in a blue moon eclipse throws a NullPointerException while editing a file. Can't reproduce it while running eclipse in a debugger,
	 * but at least this way we patch the problem to be a bit more specific in the error that should then appear.
	 */
	public static boolean debugPrintStateOfScope(Object in) throws Exception {
		/* this.scope.enclosingSourceType().sourceName */
		Object scope = in.getClass().getField("scope").get(in);
		String msg = null;
		if (scope == null) msg = "scope itself is null";
		else {
			Object sourceTypeBinding = scope.getClass().getMethod("enclosingSourceType").invoke(scope);
			if (sourceTypeBinding == null) msg = "scope.enclosingSourceType() is null";
		}
		
		if (msg != null) throw new NullPointerException(msg);
		return false;
	}
	
	public static boolean skipRewritingGeneratedNodes(org.eclipse.jdt.core.dom.ASTNode node) throws Exception {
		return ((Boolean)node.getClass().getField("$isGenerated").get(node)).booleanValue();
	}
	
	public static void setIsGeneratedFlag(org.eclipse.jdt.core.dom.ASTNode domNode,
			org.eclipse.jdt.internal.compiler.ast.ASTNode internalNode) throws Exception {
		if (internalNode == null || domNode == null) return;
		boolean isGenerated = internalNode.getClass().getField("$generatedBy").get(internalNode) != null;
		if (isGenerated) {
			domNode.getClass().getField("$isGenerated").set(domNode, true);
			domNode.setFlags(domNode.getFlags() & ~org.eclipse.jdt.core.dom.ASTNode.ORIGINAL);
		}
	}
	
	public static void setIsGeneratedFlagForSimpleName(SimpleName name, Object internalNode) throws Exception {
		if (internalNode instanceof org.eclipse.jdt.internal.compiler.ast.ASTNode) {
			if (internalNode.getClass().getField("$generatedBy").get(internalNode) != null) {
				name.getClass().getField("$isGenerated").set(name, true);
			}
		}
	}
	
	public static IMethod[] removeGeneratedMethods(IMethod[] methods) throws Exception {
		List<IMethod> result = new ArrayList<IMethod>();
		for (IMethod m : methods) {
			if (m.getNameRange().getLength() > 0) result.add(m);
		}
		return result.size() == methods.length ? methods : result.toArray(new IMethod[0]);
	}
	
	public static SimpleName[] removeGeneratedSimpleNames(SimpleName[] in) throws Exception {
		Field f = SimpleName.class.getField("$isGenerated");
		
		int count = 0;
		for (int i = 0; i < in.length; i++) {
			if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) count++;
		}
		if (count == in.length) return in;
		SimpleName[] newSimpleNames = new SimpleName[count];
		count = 0;
		for (int i = 0; i < in.length; i++) {
			if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) newSimpleNames[count++] = in[i];
		}
		return newSimpleNames;
	}
	
	public static byte[] runPostCompiler(byte[] bytes, String fileName) {
		byte[] transformed = PostCompiler.applyTransformations(bytes, fileName, DiagnosticsReceiver.CONSOLE);
		return transformed == null ? bytes : transformed;
	}
	
	public static OutputStream runPostCompiler(OutputStream out) throws IOException {
		return PostCompiler.wrapOutputStream(out, "TEST", DiagnosticsReceiver.CONSOLE);
	}
	
	public static BufferedOutputStream runPostCompiler(BufferedOutputStream out, String path, String name) throws IOException {
		String fileName = path + "/" + name;
		return new BufferedOutputStream(PostCompiler.wrapOutputStream(out, fileName, DiagnosticsReceiver.CONSOLE));
	}
	
	public static void copyInitializationOfLocalDeclarationForVal(Parser parser) {
		ASTNode[] astStack;
		int astPtr;
		try {
			Field astStackF = Parser.class.getDeclaredField("astStack");
			astStackF.setAccessible(true);
			astStack = (ASTNode[]) astStackF.get(parser);
			Field astPtrF = Parser.class.getDeclaredField("astPtr");
			astPtrF.setAccessible(true);
			astPtr = (Integer)astPtrF.get(parser);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration) astStack[astPtr];
		if (!(variableDecl instanceof LocalDeclaration)) return;
		ASTNode init = variableDecl.initialization;
		if (init == null) return;
		if (variableDecl.type instanceof SingleTypeReference) {
			SingleTypeReference ref = (SingleTypeReference) variableDecl.type;
			if (ref.token == null || ref.token.length != 3 || ref.token[0] != 'v' || ref.token[1] != 'a' || ref.token[2] != 'l') return;
		} else return;
		
		try {
			if (initCopyField != null) initCopyField.set(variableDecl, init);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			// In ecj mode this field isn't there and we don't need the copy anyway, so, we ignore the exception.
		}
	}
	
	private static Field initCopyField;
	
	static {
		try {
			initCopyField = LocalDeclaration.class.getDeclaredField("$initCopy");
		} catch (Throwable t) {
			 //ignore - no $initCopy exists when running in ecj.
		}
	}
	
	public static boolean handleValForLocalDeclaration(LocalDeclaration local, BlockScope scope) {
		if (local.type instanceof SingleTypeReference) {
			char[] token = ((SingleTypeReference)local.type).token;
			if (token == null || token.length != 3) return false;
			else if (token[0] != 'v' || token[1] != 'a' || token[2] != 'l') return false;
		} else return false;
		
		Expression init = local.initialization;
		if (init == null && initCopyField != null) {
			try {
				init = (Expression) initCopyField.get(local);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		TypeReference replacement = null;
		if (init != null) {
			TypeBinding resolved = init.resolveType(scope);
			if (resolved != null) {
				replacement = Eclipse.makeType(resolved, local.type, false);
			}
		}
		
		local.modifiers |= ClassFileConstants.AccFinal;
		local.type = replacement != null ? replacement : new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, Eclipse.poss(local.type, 3));
		
		return false;
	}
	
	public static TypeBinding skipResolveInitializerIfAlreadyCalled(Expression expr, BlockScope scope) {
		if (expr.resolvedType != null) return expr.resolvedType;
		return expr.resolveType(scope);
	}
}
