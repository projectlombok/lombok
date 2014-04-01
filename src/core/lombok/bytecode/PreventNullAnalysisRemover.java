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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@ProviderFor(PostCompilerTransformation.class)
public class PreventNullAnalysisRemover implements PostCompilerTransformation {
	
	@Override public byte[] applyTransformations(byte[] original, String fileName, DiagnosticsReceiver diagnostics) {
		if (!new ClassFileMetaData(original).usesMethod("lombok/Lombok", "preventNullAnalysis")) return null;
		
		byte[] fixedByteCode = fixJSRInlining(original);
		
		ClassReader reader = new ClassReader(fixedByteCode);
		ClassWriter writer = new FixedClassWriter(reader, 0);
		
		final AtomicBoolean changesMade = new AtomicBoolean();
		
		class PreventNullAnalysisVisitor extends MethodVisitor {
			PreventNullAnalysisVisitor(MethodVisitor mv) {
				super(Opcodes.ASM5, mv);
			}
			
			@Override public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				boolean hit = true;
				if (hit && opcode != Opcodes.INVOKESTATIC) hit = false;
				if (hit && !"preventNullAnalysis".equals(name)) hit = false;
				if (hit && !"lombok/Lombok".equals(owner)) hit = false;
				if (hit && !"(Ljava/lang/Object;)Ljava/lang/Object;".equals(desc)) hit = false;
				if (hit) {
					changesMade.set(true);
					if (System.getProperty("lombok.debugAsmOnly", null) != null) super.visitMethodInsn(opcode, owner, name, desc, itf); // DEBUG for issue 470!
				} else {
					super.visitMethodInsn(opcode, owner, name, desc, itf);
				}
			}
		}
		
		reader.accept(new ClassVisitor(Opcodes.ASM5, writer) {
			@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				return new PreventNullAnalysisVisitor(super.visitMethod(access, name, desc, signature, exceptions));
			}
		}, 0);
		return changesMade.get() ? writer.toByteArray() : null;
	}
}
