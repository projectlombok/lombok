/*
 * Copyright (C) 2010-2014 The Project Lombok Authors.
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

import static lombok.bytecode.AsmUtil.*;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompilerTransformation;

import org.mangosdk.spi.ProviderFor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@ProviderFor(PostCompilerTransformation.class)
public class SneakyThrowsRemover implements PostCompilerTransformation {
	
	@Override public byte[] applyTransformations(byte[] original, String fileName, final DiagnosticsReceiver diagnostics) {
		if (!new ClassFileMetaData(original).usesMethod("lombok/Lombok", "sneakyThrow")) return null;
		
		byte[] fixedByteCode = fixJSRInlining(original);
		
		ClassReader reader = new ClassReader(fixedByteCode);
		ClassWriter writer = new ClassWriter(reader, 0);

		final AtomicBoolean changesMade = new AtomicBoolean();
		
		class SneakyThrowsRemoverVisitor extends MethodVisitor {
			SneakyThrowsRemoverVisitor(MethodVisitor mv) {
				super(Opcodes.ASM7, mv);
			}
			
			private boolean methodInsnQueued = false;
			
			@Override public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				if (
						opcode == Opcodes.INVOKESTATIC &&
						"sneakyThrow".equals(name) &&
						"lombok/Lombok".equals(owner) &&
						"(Ljava/lang/Throwable;)Ljava/lang/RuntimeException;".equals(desc)) {
					
					if (System.getProperty("lombok.debugAsmOnly", null) != null) {
						super.visitMethodInsn(opcode, owner, name, desc, itf); // DEBUG for issue 470!
					} else {
						methodInsnQueued = true;
					}
				} else {
					super.visitMethodInsn(opcode, owner, name, desc, itf);
				}
			}
			
			private void handleQueue() {
				if (!methodInsnQueued) return;
				super.visitMethodInsn(Opcodes.INVOKESTATIC, "lombok/Lombok", "sneakyThrow", "(Ljava/lang/Throwable;)Ljava/lang/RuntimeException;", false);
				methodInsnQueued = false;
				diagnostics.addWarning("Proper usage is: throw lombok.Lombok.sneakyThrow(someException);. You did not 'throw' it. Because of this, the call to sneakyThrow " +
						"remains in your classfile and you will need lombok.jar on the classpath at runtime.");
			}
			
			@Override public void visitInsn(int arg0) {
				if (methodInsnQueued && arg0 == Opcodes.ATHROW) {
					changesMade.set(true);
					// As expected, the required ATHROW. We can now safely 'eat' the previous call.
					methodInsnQueued = false;
				}
				handleQueue();
				super.visitInsn(arg0);
			}
			@Override public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
				handleQueue();
				super.visitFrame(arg0, arg1, arg2, arg3, arg4);
			}
			
			@Override public void visitIincInsn(int arg0, int arg1) {
				handleQueue();
				super.visitIincInsn(arg0, arg1);
			}
			
			@Override public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
				handleQueue();
				super.visitFieldInsn(arg0, arg1, arg2, arg3);
			}
			
			@Override public void visitIntInsn(int arg0, int arg1) {
				handleQueue();
				super.visitIntInsn(arg0, arg1);
			}
			
			@Override public void visitEnd() {
				handleQueue();
				super.visitEnd();
			}
			
			@Override public void visitInvokeDynamicInsn(String arg0, String arg1, Handle arg2, Object... arg3) {
				handleQueue();
				super.visitInvokeDynamicInsn(arg0, arg1, arg2, arg3);
			}
			
			@Override public void visitLabel(Label arg0) {
				handleQueue();
				super.visitLabel(arg0);
			}
			
			@Override public void visitJumpInsn(int arg0, Label arg1) {
				handleQueue();
				super.visitJumpInsn(arg0, arg1);
			}
			
			@Override public void visitLdcInsn(Object arg0) {
				handleQueue();
				super.visitLdcInsn(arg0);
			}
			
			@Override public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3, Label arg4, int arg5) {
				handleQueue();
				super.visitLocalVariable(arg0, arg1, arg2, arg3, arg4, arg5);
			}
			
			@Override public void visitMaxs(int arg0, int arg1) {
				handleQueue();
				super.visitMaxs(arg0, arg1);
			}
			
			@Override public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
				handleQueue();
				super.visitLookupSwitchInsn(arg0, arg1, arg2);
			}
			
			@Override public void visitMultiANewArrayInsn(String arg0, int arg1) {
				handleQueue();
				super.visitMultiANewArrayInsn(arg0, arg1);
			}
			
			@Override public void visitVarInsn(int arg0, int arg1) {
				handleQueue();
				super.visitVarInsn(arg0, arg1);
			}
			
			@Override public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
				handleQueue();
				super.visitTryCatchBlock(arg0, arg1, arg2, arg3);
			}
			
			@Override public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label... arg3) {
				handleQueue();
				super.visitTableSwitchInsn(arg0, arg1, arg2, arg3);
			}
			
			@Override public void visitTypeInsn(int arg0, String arg1) {
				handleQueue();
				super.visitTypeInsn(arg0, arg1);
			}
		}
		
		reader.accept(new ClassVisitor(Opcodes.ASM7, writer) {
			@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				return new SneakyThrowsRemoverVisitor(super.visitMethod(access, name, desc, signature, exceptions));
			}
		}, 0);
		return changesMade.get() ? writer.toByteArray() : null;
	}
}
