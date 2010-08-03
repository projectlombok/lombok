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
package lombok.bytecode;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompilerTransformation;

import org.mangosdk.spi.ProviderFor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

@ProviderFor(PostCompilerTransformation.class)
public class SneakyThrowsRemover implements PostCompilerTransformation {
	private static class FixedClassWriter extends ClassWriter {
		FixedClassWriter(ClassReader classReader, int flags) {
			super(classReader, flags);
		}
		
		@Override protected String getCommonSuperClass(String type1, String type2) {
			//By default, ASM will attempt to live-load the class types, which will fail if meddling with classes in an
			//environment with custom classloaders, such as Equinox. It's just an optimization; returning Object is always legal.
			try {
				return super.getCommonSuperClass(type1, type2);
			} catch (Exception e) {
				return "java/lang/Object";
			}
		}
	}
	
	protected byte[] fixJSRInlining(byte[] byteCode) {
		ClassReader reader = new ClassReader(byteCode);
		ClassWriter writer = new FixedClassWriter(reader, 0);
		
		ClassVisitor visitor = new ClassAdapter(writer) {
			@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				return new JSRInlinerAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc, signature, exceptions);
			}
		};
		
		reader.accept(visitor, 0);
		return writer.toByteArray();
	}
	
	@Override public byte[] applyTransformations(byte[] original, String className, DiagnosticsReceiver diagnostics) {
		byte[] fixedByteCode = fixJSRInlining(original);
		
		ClassReader reader = new ClassReader(fixedByteCode);
		ClassWriter writer = new FixedClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		
		final AtomicBoolean changesMade = new AtomicBoolean();
		
		class SneakyThrowsRemoverVisitor extends MethodAdapter {
			SneakyThrowsRemoverVisitor(MethodVisitor mv) {
				super(mv);
			}
			
			@Override public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				boolean hit = true;
				if (hit && opcode != Opcodes.INVOKESTATIC) hit = false;
				if (hit && !"sneakyThrow".equals(name)) hit = false;
				if (hit && !"lombok/Lombok".equals(owner)) hit = false;
				if (hit && !"(Ljava/lang/Throwable;)Ljava/lang/RuntimeException;".equals(desc)) hit = false;
				if (hit) {
					changesMade.set(true);
					super.visitInsn(Opcodes.ATHROW);
				} else {
					super.visitMethodInsn(opcode, owner, name, desc);
				}
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
