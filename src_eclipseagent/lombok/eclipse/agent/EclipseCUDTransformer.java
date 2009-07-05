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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Transforms Eclipse's <code>org.eclipse.jdt.internal.compiler.ast CompilationUnitDeclaration</code> class,
 * which is the top-level AST Node class for Eclipse.
 * 
 * Transformations applied:<ul>
 * <li>A field is added: 'public transient Object $lombokAST = null;'. We use it to cache our own AST object,
 * usually of type <code>lombok.eclipse.EclipseAST.</code></li></ul>
 */
class EclipseCUDTransformer {
	byte[] transform(byte[] classfileBuffer) {
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(reader, 0);
	
		ClassAdapter adapter = new CUDPatcherAdapter(writer);
		reader.accept(adapter, 0);
		return writer.toByteArray();
	}
	
	private static class CUDPatcherAdapter extends ClassAdapter {
		CUDPatcherAdapter(ClassVisitor cv) {
			super(cv);
		}
		
		@Override public void visitEnd() {
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_TRANSIENT, "$lombokAST", "Ljava/lang/Object;", null, null);
			fv.visitEnd();
			cv.visitEnd();
		}
	}
}
