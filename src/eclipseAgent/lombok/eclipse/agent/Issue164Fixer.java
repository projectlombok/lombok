package lombok.eclipse.agent;

import lombok.core.debug.DebugSnapshotStore;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class Issue164Fixer {
	public static byte[] fix(byte[] classfileBuffer) {
		return runAsm(classfileBuffer, true);
	}
	
	static class FixedClassWriter extends ClassWriter {
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
	
	public static void catchScopeSet(TypeDeclaration typeDeclaration, ClassScope scope) {
		typeDeclaration.scope = scope;
		Scope sc = scope;
		while (sc != null && !(sc instanceof CompilationUnitScope)) {
			sc = sc.parent;
		}
		
		if (sc instanceof CompilationUnitScope) {
			CompilationUnitDeclaration cud = ((CompilationUnitScope) sc).referenceContext;
			DebugSnapshotStore.INSTANCE.snapshot(cud, "Scope is being set");
		}
	}
	
	/**
	 * Runs ASM on the provider byteCode, chaining a reader to a writer and using the {@code ClassVisitor} you yourself provide
	 * via the {@see #createClassVisitor(ClassWriter)} method as the filter.
	 */
	protected static byte[] runAsm(byte[] byteCode, boolean computeFrames) {
		byte[] fixedByteCode = fixJSRInlining(byteCode);
		
		ClassReader reader = new ClassReader(fixedByteCode);
		ClassWriter writer = new FixedClassWriter(reader, computeFrames ? ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES : 0);
		
		ClassVisitor visitor = new ClassAdapter(writer) {
			@Override public MethodVisitor visitMethod(int access, final String mName, String desc, String signature, String[] exceptions) {
				return new MethodAdapter(super.visitMethod(access, mName, desc, signature, exceptions)) {
					@Override public void visitFieldInsn(int opcode, String owner, String name, String desc) {
						if (opcode == Opcodes.PUTFIELD && "org/eclipse/jdt/internal/compiler/ast/TypeDeclaration".equals(owner) && "scope".equals(name) && !"catchScopeSet".equals(mName)) {
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "lombok/eclipse/agent/Issue164Fixer", "catchScopeSet", "(Lorg/eclipse/jdt/internal/compiler/ast/TypeDeclaration;Lorg/eclipse/jdt/internal/compiler/lookup/ClassScope;)V");
						} else {
							super.visitFieldInsn(opcode, owner, name, desc);
						}
					}
				};
			}
		};
		reader.accept(visitor, 0);
		return writer.toByteArray();
	}
	
	protected static byte[] fixJSRInlining(byte[] byteCode) {
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
}
