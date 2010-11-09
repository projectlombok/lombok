/*
 * Copyright © 2010 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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
package lombok.bytecode;

import static lombok.bytecode.PostCompilationUtil.*;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompilerTransformation;

import org.mangosdk.spi.ProviderFor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@ProviderFor(PostCompilerTransformation.class)
public class SneakyThrowsRemover implements PostCompilerTransformation {
	
	@Override public byte[] applyTransformations(byte[] original, String fileName, DiagnosticsReceiver diagnostics) {
		if (!new ClassFileMetaData(original).usesMethod("lombok/Lombok", "sneakyThrow")) return null;
		
		byte[] fixedByteCode = fixJSRInlining(original);
		
		ClassReader reader = new ClassReader(fixedByteCode);
		ClassWriter writer = new FixedClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		
		final AtomicBoolean changesMade = new AtomicBoolean();
		
		class SneakyThrowsRemoverVisitor extends MethodAdapter {
			boolean justAddedAthrow = false;
			
			SneakyThrowsRemoverVisitor(MethodVisitor mv) {
				super(mv);
			}
			
			@Override public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				justAddedAthrow = false;
				boolean hit = true;
				if (hit && opcode != Opcodes.INVOKESTATIC) hit = false;
				if (hit && !"sneakyThrow".equals(name)) hit = false;
				if (hit && !"lombok/Lombok".equals(owner)) hit = false;
				if (hit && !"(Ljava/lang/Throwable;)Ljava/lang/RuntimeException;".equals(desc)) hit = false;
				if (hit) {
					changesMade.set(true);
					justAddedAthrow = true;
					super.visitInsn(Opcodes.ATHROW);
				} else {
					super.visitMethodInsn(opcode, owner, name, desc);
				}
			}
			
			@Override public void visitInsn(int opcode) {
				if (!justAddedAthrow || opcode != Opcodes.ATHROW) {
					super.visitInsn(opcode);
				}
				justAddedAthrow = false;
			}
			
			@Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				justAddedAthrow = false;
				return super.visitAnnotation(desc, visible);
			}
			
			@Override public AnnotationVisitor visitAnnotationDefault() {
				justAddedAthrow = false;
				return super.visitAnnotationDefault();
			}
			
			@Override public void visitAttribute(Attribute attr) {
				justAddedAthrow = false;
				super.visitAttribute(attr);
			}
			
			@Override public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				justAddedAthrow = false;
				super.visitFieldInsn(opcode, owner, name, desc);
			}
			
			@Override public void visitIincInsn(int var, int increment) {
				justAddedAthrow = false;
				super.visitIincInsn(var, increment);
			}
			
			@Override public void visitIntInsn(int opcode, int operand) {
				justAddedAthrow = false;
				super.visitIntInsn(opcode, operand);
			}
			
			@Override public void visitJumpInsn(int opcode, Label label) {
				justAddedAthrow = false;
				super.visitJumpInsn(opcode, label);
			}
			
			@Override public void visitLabel(Label label) {
				justAddedAthrow = false;
				super.visitLabel(label);
			}
			
			@Override public void visitLdcInsn(Object cst) {
				justAddedAthrow = false;
				super.visitLdcInsn(cst);
			}
			
			@Override public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
				justAddedAthrow = false;
				super.visitLocalVariable(name, desc, signature, start, end, index);
			}
			
			@Override public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
				justAddedAthrow = false;
				super.visitLookupSwitchInsn(dflt, keys, labels);
			}
			
			@Override public void visitMultiANewArrayInsn(String desc, int dims) {
				justAddedAthrow = false;
				super.visitMultiANewArrayInsn(desc, dims);
			}
			
			@Override public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
				justAddedAthrow = false;
				return super.visitParameterAnnotation(parameter, desc, visible);
			}
			
			@Override public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
				justAddedAthrow = false;
				super.visitTableSwitchInsn(min, max, dflt, labels);
			}
			
			@Override public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
				justAddedAthrow = false;
				super.visitTryCatchBlock(start, end, handler, type);
			}
			
			@Override public void visitTypeInsn(int opcode, String type) {
				justAddedAthrow = false;
				super.visitTypeInsn(opcode, type);
			}
			
			@Override public void visitVarInsn(int opcode, int var) {
				justAddedAthrow = false;
				super.visitVarInsn(opcode, var);
			}
		}
		
		reader.accept(new ClassAdapter(writer) {
			@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				return new SneakyThrowsRemoverVisitor(super.visitMethod(access, name, desc, signature, exceptions));
			}
		}, 0);
		return changesMade.get() ? writer.toByteArray() : null;
	}
}
