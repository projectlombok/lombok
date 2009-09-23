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
package lombok.eclipse.agent;

import org.mangosdk.spi.ProviderFor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Transforms Eclipse's {@code org.eclipse.jdt.internal.compiler.ast.ASTNode} class,
 * which is the super-class of all AST Node class for Eclipse.
 * 
 * Transformations applied:<ul>
 * <li>A field is added: 'public transient ASTNode $generatedBy = null;'. It is set to something other than {@code null} if
 * this node is generated; the reference then points at the node that is responsible for its generation (example: a {@code @Data} annotation).</li></ul>
 */
@ProviderFor(EclipseTransformer.class)
public class EclipseASTNodeTransformer implements EclipseTransformer {
	private static final String ASTNODE = "org/eclipse/jdt/internal/compiler/ast/ASTNode";

	public byte[] transform(byte[] classfileBuffer) {
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(reader, 0);
	
		ClassAdapter adapter = new ASTNodePatcherAdapter(writer);
		reader.accept(adapter, 0);
		return writer.toByteArray();
	}
	
	private static class ASTNodePatcherAdapter extends ClassAdapter {
		ASTNodePatcherAdapter(ClassVisitor cv) {
			super(cv);
		}
		
		@Override public void visitEnd() {
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_TRANSIENT, "$generatedBy", "L" + ASTNODE + ";", null, null);
			fv.visitEnd();
			cv.visitEnd();
		}
	}
	
	@Override public String getTargetClassName() {
		return ASTNODE;
	}
}
