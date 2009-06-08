package lombok.agent.eclipse;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class EclipseParserTransformer {
	private static final String COMPILATION_UNIT_DECLARATION_SIG =
		"Lorg/eclipse/jdt/internal/compiler/ast/CompilationUnitDeclaration;";
	
	private static final String TOPATCH_METHOD_NAME = "endParse";
	private static final String TOPATCH_METHOD_DESC = "(I)" + COMPILATION_UNIT_DECLARATION_SIG;
	
	private final byte[] in;
	
	EclipseParserTransformer(byte[] classfileBuffer) {
		in = classfileBuffer;
	}
	
	byte[] transform() {
		ClassReader reader = new ClassReader(in);
		ClassWriter writer = new ClassWriter(reader, 0);
		ClassAdapter adapter = new ParserPatcherAdapter(writer);
		reader.accept(adapter, 0);
		return writer.toByteArray();
	}
	
	private static class ParserPatcherAdapter extends ClassAdapter {
		public ParserPatcherAdapter(ClassVisitor cv) {
			super(cv);
		}
		
		@Override public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			MethodVisitor writerVisitor = super.visitMethod(access, name, desc, signature, exceptions);
			if ( !TOPATCH_METHOD_NAME.equals(name) || !TOPATCH_METHOD_DESC.equals(desc) ) return writerVisitor;
			
			return new PatcherMethodVisitor(writerVisitor);
		}
	}
	
	static class PatcherMethodVisitor extends MethodAdapter {
		private static final String TARGET_STATIC_CLASS = "java/lombok/ClassLoaderWorkaround";
		private static final String TARGET_STATIC_METHOD_NAME = "transformCompilationUnitDeclaration";
		private static final String TARGET_STATIC_METHOD_DESC = "(Ljava/lang/Object;)V";
		
		private boolean alreadyCalled = false;
		
		PatcherMethodVisitor(MethodVisitor mv) {
			super(mv);
		}
		
		@Override public void visitInsn(int opcode) {
			if ( opcode == Opcodes.ARETURN ) insertHookCall();
			
			super.visitInsn(opcode);
		}
		
		@Override public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			if ( opcode == Opcodes.INVOKESTATIC &&
					TARGET_STATIC_CLASS.equals(owner) && TARGET_STATIC_METHOD_NAME.equals(name) ) alreadyCalled = true;
			super.visitMethodInsn(opcode, owner, name, desc);
		}
		
		/** When this method is called, the stack should hold the reference to the
		 * just-parsed CompilationUnitDeclaration object that is about to be returned
		 * to whomever wants it. We will put a call to a method of our choosing in,
		 * which will transform the CUD. The stack is not modified (that is, that method
		 * returns a CUD).
		 */
		private void insertHookCall() {
			if ( alreadyCalled ) return;
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESTATIC, TARGET_STATIC_CLASS,
					TARGET_STATIC_METHOD_NAME, TARGET_STATIC_METHOD_DESC);
		}
	}
}
