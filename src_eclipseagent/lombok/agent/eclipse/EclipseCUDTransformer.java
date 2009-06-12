package lombok.agent.eclipse;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

class EclipseCUDTransformer {
	private final byte[] in;

	EclipseCUDTransformer(byte[] classfileBuffer) {
		in = classfileBuffer;
	}

	byte[] transform() {
		ClassReader reader = new ClassReader(in);
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
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC, "$lombokAST", "Ljava/lang/Object;", null, null);
			fv.visitEnd();
			cv.visitEnd();
		}
	}
}
