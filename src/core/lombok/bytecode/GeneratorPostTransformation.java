/*
 * Copyright (C) 2023 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompilerTransformation;
import lombok.spi.Provides;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

@Provides
public class GeneratorPostTransformation implements PostCompilerTransformation {
    private static final String GENERATOR_ERR_RESUME_ON_FINISH = "Called next on finished generator";
    private static final String GENERATOR_ERR_UNREACHABLE = "Unreachable generator state";

	@Override public byte[] applyTransformations(byte[] original, String fileName, final DiagnosticsReceiver diagnostics) {
		if (!"lombok/Lombok$Generator".equals(new ClassFileMetaData(original).getSuperClassName())) return null;

		byte[] fixedByteCode = fixJSRInlining(original);
		
		ClassReader reader = new ClassReader(fixedByteCode);
		ClassWriter writer = new ClassWriter(
            reader,
            ClassWriter.COMPUTE_MAXS
        );

        GeneratorClass generatorClass = initializeClass(Type.getType(Object.class), reader.getClassName(), writer, setupTable(reader));

        reader.accept(transformClass(writer, reader.getClassName(), generatorClass), 0);

        return writer.toByteArray();
	}

    private static boolean isYieldThis(int opcode, String className, String owner, String name) {
        return opcode == Opcodes.INVOKEVIRTUAL && "yieldThis".equals(name) && owner.equals(className);
    }

    private static boolean isYieldAll(int opcode, String className, String owner, String name) {
        return opcode == Opcodes.INVOKEVIRTUAL && "yieldAll".equals(name) && owner.equals(className);
    }

    private ClassVisitor transformClass(final ClassWriter writer, final String className, final GeneratorClass generatorClass) {
        return new ClassVisitor(Opcodes.ASM7, writer) {
            @Override public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                access |= Opcodes.ACC_SYNTHETIC;
                superName = Type.getInternalName(Object.class);
                interfaces = new String[] {
                    Type.getInternalName(Iterable.class),
                    Type.getInternalName(Iterator.class)
                };

                super.visit(version, access, name, signature, superName, interfaces);

                FieldNode stateNode = generatorClass.stateNode;
                FieldNode resultNode = generatorClass.resultNode;
                writer.visitField(stateNode.access, stateNode.name, stateNode.desc, stateNode.signature, stateNode.value);
                writer.visitField(resultNode.access, resultNode.name, resultNode.desc, resultNode.signature, resultNode.value);
        
                visitIterator(writer);
                visitHasNext(writer, className, resultNode);
                visitNext(writer, className, resultNode);
                visitRemove(writer);
            }

            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                if ("<init>".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM7, visitor) {
                        @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            if (opcode == Opcodes.INVOKESPECIAL && "lombok/Lombok$Generator".equals(owner)) {
                                owner = Type.getInternalName(Object.class);
                            }

                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    };
                } else if ("advance".equals(name)) {
                    return transformAdvance(writer, visitor, className, generatorClass);
                }

                return visitor;
            }
        };
    }

    private MethodVisitor transformAdvance(
        final ClassWriter writer,
        final MethodVisitor methodVisitor,
        final String className,
        final GeneratorClass generatorClass
    ) {
        final Frame<BasicValue> frame = new Frame<BasicValue>(65536, 65536);

        MethodVisitor transformer = new MethodVisitor(Opcodes.ASM7, methodVisitor) {
            private int index = 0;

            private int tmpIndex = 0;
            private Map<Integer, FieldNode> variableMap = new HashMap<Integer, FieldNode>();
            
            private void setState(int index) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                switch (index) {
                    case 0: super.visitInsn(Opcodes.ICONST_0); break;
                    case 1: super.visitInsn(Opcodes.ICONST_1); break;
                    case 2: super.visitInsn(Opcodes.ICONST_2); break;
                    case 3: super.visitInsn(Opcodes.ICONST_3); break;
                    case 4: super.visitInsn(Opcodes.ICONST_4); break;
                    case 5: super.visitInsn(Opcodes.ICONST_5); break;
                    default: {
                        if (index <= 0xff) {
                            super.visitIntInsn(Opcodes.BIPUSH, index);
                        } else if (index <= 0xffff) {
                            super.visitIntInsn(Opcodes.SIPUSH, index);
                        } else {
                            super.visitLdcInsn(index);
                        }
                    }
                }
                
                super.visitFieldInsn(Opcodes.PUTFIELD, className, generatorClass.stateNode.name, generatorClass.stateNode.desc);
            }

            @Override public void visitCode() {
                super.visitCode();

                Label[] labels = generatorClass.table.toLabels();
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, className, generatorClass.stateNode.name, generatorClass.stateNode.desc);
                super.visitTableSwitchInsn(0, labels.length - 1, generatorClass.table.defaultLabel, labels);
                super.visitLabel(generatorClass.table.start);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }

            private Type getVariableType(int opcode, int varIndex) {
                switch (opcode) {
                    case Opcodes.ISTORE:
                    case Opcodes.ILOAD: return Type.INT_TYPE;

                    case Opcodes.FLOAD:
                    case Opcodes.FSTORE: return Type.FLOAT_TYPE;

                    case Opcodes.DLOAD:
                    case Opcodes.DSTORE: return Type.DOUBLE_TYPE;

                    case Opcodes.LLOAD:
                    case Opcodes.LSTORE: return Type.LONG_TYPE;

                    default: {
                        BasicValue val = frame.getLocal(varIndex);
                        if (val != null) {
                            return val.getType();
                        }

                        return Type.getType(Object.class);
                    }
                }
            }

            private FieldNode createTempVariable(String descriptor) {
                FieldNode node = new FieldNode(Opcodes.ACC_SYNTHETIC, "gen$" + tmpIndex++, descriptor, null, null);
                writer.visitField(node.access, node.name, descriptor, null, null);
                return node;
            }

            @Override public void visitVarInsn(int opcode, int varIndex) {
                switch (opcode) {
                    case Opcodes.ASTORE:
                    case Opcodes.ISTORE:
                    case Opcodes.LSTORE:
                    case Opcodes.FSTORE:
                    case Opcodes.DSTORE: {
                        String descriptor = getVariableType(opcode, varIndex).getDescriptor();

                        super.visitVarInsn(Opcodes.ALOAD, 0);

                        FieldNode old = variableMap.get(varIndex);
                        FieldNode node;
                        if (old != null && descriptor.equals(old.desc)) {
                            node = old;
                        } else {
                            if (old != null && Type.getType(old.desc).getSort() == Type.OBJECT) {
                                super.visitInsn(Opcodes.DUP);
                                super.visitInsn(Opcodes.ACONST_NULL);
                                super.visitFieldInsn(Opcodes.PUTFIELD, className, old.name, old.desc);
                            }

                            node = createTempVariable(descriptor);
                            variableMap.put(varIndex, node);
                        }

                        super.visitInsn(Opcodes.SWAP);
                        super.visitFieldInsn(Opcodes.PUTFIELD, className, node.name, descriptor);
                        return;
                    }

                    case Opcodes.ALOAD:
                    case Opcodes.ILOAD:
                    case Opcodes.LLOAD:
                    case Opcodes.FLOAD:
                    case Opcodes.DLOAD: {
                        FieldNode node = variableMap.get(varIndex);
                        if (node == null) {
                            break;
                        }

                        super.visitVarInsn(Opcodes.ALOAD, 0);
                        super.visitFieldInsn(Opcodes.GETFIELD, className, node.name, node.desc);
                        return;
                    }
                }

                super.visitVarInsn(opcode, varIndex);
            }

            @Override public void visitIincInsn(int varIndex, int increment) {
                FieldNode node = variableMap.get(varIndex);
                if (node == null) {
                    super.visitIincInsn(varIndex, increment);
                    return;
                }

                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.DUP);
                super.visitFieldInsn(Opcodes.GETFIELD, className, node.name, node.desc);
                switch (increment) {
                    case -1: super.visitInsn(Opcodes.ICONST_M1); break;
                    case 0: super.visitInsn(Opcodes.ICONST_0); break;
                    case 1: super.visitInsn(Opcodes.ICONST_1); break;
                    case 2: super.visitInsn(Opcodes.ICONST_2); break;
                    case 3: super.visitInsn(Opcodes.ICONST_3); break;
                    case 4: super.visitInsn(Opcodes.ICONST_4); break;
                    case 5: super.visitInsn(Opcodes.ICONST_5); break;
                    default: super.visitIntInsn(Opcodes.BIPUSH, increment); break;
                }
                super.visitInsn(Opcodes.IADD);

                super.visitFieldInsn(Opcodes.PUTFIELD, className, node.name, node.desc);
            }

            @Override public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
                if (local != null) {
                    // Remove all local variables
                    if (type == Opcodes.F_FULL || type == Opcodes.F_NEW) {
                        numLocal = 1;
                    } else if (type == Opcodes.F_APPEND || type == Opcodes.F_CHOP) {
                        numLocal = 0;
                    }
                }

                super.visitFrame(type, numLocal, local, numStack, stack);
            }

            private void expandYieldThis() {
                Label next = generatorClass.table.yields[index++];

                // Assign next value (value already loaded by method invocation)
                super.visitFieldInsn(Opcodes.PUTFIELD, className, generatorClass.resultNode.name, generatorClass.resultNode.desc);
                // Assign state
                setState(index);
                super.visitInsn(Opcodes.RETURN);

                // Mark next
                super.visitLabel(next);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }

            private void expandYieldAllIterable() {
                Label next = generatorClass.table.yields[index++];

                FieldNode iteratorNode = createTempVariable(Type.getDescriptor(Iterator.class));

                // Create iterator (iterable already loaded by method invocation)
                super.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    Type.getInternalName(Iterable.class),
                    "iterator",
                    "()Ljava/util/Iterator;",
                    true
                );
                super.visitFieldInsn(Opcodes.PUTFIELD, className, iteratorNode.name, iteratorNode.desc);

                // Mark next
                setState(index);
                super.visitLabel(next);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                // Jump to next on end
                Label loopEnd = new Label();
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, className, iteratorNode.name, iteratorNode.desc);
                super.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    Type.getInternalName(Iterator.class),
                    "hasNext",
                    "()Z",
                    true
                );
                super.visitJumpInsn(Opcodes.IFEQ, loopEnd);

                // Assign next value
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.DUP);
                super.visitFieldInsn(Opcodes.GETFIELD, className, iteratorNode.name, iteratorNode.desc);
                super.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    Type.getInternalName(Iterator.class),
                    "next",
                    "()Ljava/lang/Object;",
                    true
                );
                super.visitFieldInsn(Opcodes.PUTFIELD, className, generatorClass.resultNode.name, generatorClass.resultNode.desc);
                super.visitInsn(Opcodes.RETURN);
            
                super.visitLabel(loopEnd);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                // Cleanup iterator
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.ACONST_NULL);
                super.visitFieldInsn(Opcodes.PUTFIELD, className, iteratorNode.name, iteratorNode.desc);
            }

            private void expandYieldAllArray() {
                Label next = generatorClass.table.yields[index++];

                FieldNode arrayNode = createTempVariable("[" + generatorClass.type.getDescriptor());
                FieldNode lengthNode = createTempVariable(Type.INT_TYPE.getDescriptor());
                FieldNode incrementNode = createTempVariable(Type.INT_TYPE.getDescriptor());
                Label loopEnd = new Label();

                // Initialize array
                super.visitInsn(Opcodes.DUP2);
                super.visitFieldInsn(Opcodes.PUTFIELD, className, arrayNode.name, arrayNode.desc);

                // Initialize length
                super.visitInsn(Opcodes.ARRAYLENGTH);
                super.visitFieldInsn(Opcodes.PUTFIELD, className, lengthNode.name, lengthNode.desc);

                // Initialize increment
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.ICONST_0);
                super.visitFieldInsn(Opcodes.PUTFIELD, className, incrementNode.name, incrementNode.desc);

                // Mark next
                setState(index);
                super.visitLabel(next);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                // Check increment
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, className, incrementNode.name, incrementNode.desc);
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, className, lengthNode.name, lengthNode.desc);
                super.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);

                // get value from array
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.DUP);
                super.visitFieldInsn(Opcodes.GETFIELD, className, arrayNode.name, arrayNode.desc);
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, className, incrementNode.name, incrementNode.desc);
                super.visitInsn(Opcodes.AALOAD);
                super.visitFieldInsn(Opcodes.PUTFIELD, className, generatorClass.resultNode.name, generatorClass.resultNode.desc);
                
                // add increment by 1 and return
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.DUP);
                super.visitFieldInsn(Opcodes.GETFIELD, className, incrementNode.name, incrementNode.desc);
                super.visitInsn(Opcodes.ICONST_1);
                super.visitInsn(Opcodes.IADD);
                super.visitFieldInsn(Opcodes.PUTFIELD, className, incrementNode.name, incrementNode.desc);
                super.visitInsn(Opcodes.RETURN);

                super.visitLabel(loopEnd);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                // Cleanup array
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.ACONST_NULL);
                super.visitFieldInsn(Opcodes.PUTFIELD, className, arrayNode.name, arrayNode.desc);
            }

            @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                if (isYieldThis(opcode, className, owner, name)) {
                    expandYieldThis();
                    return;
                } else if (isYieldAll(opcode, className, owner, name)) {
                    Type[] arguments = Type.getArgumentTypes(descriptor);
                    if (arguments[0].getSort() != Type.ARRAY) {
                        expandYieldAllIterable();
                    } else {
                        expandYieldAllArray();
                    }
                    return;
                }

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override public void visitInsn(int opcode) {
                switch(opcode) {
                    // Mark generator finished on return
                    case Opcodes.IRETURN:
                    case Opcodes.FRETURN:
                    case Opcodes.ARETURN:
                    case Opcodes.LRETURN:
                    case Opcodes.DRETURN:
                    case Opcodes.RETURN: {
                        setState(generatorClass.table.getFinishState());
                        break;
                    }

                    default: break;
                }

                super.visitInsn(opcode);
            }

            @Override public void visitMaxs(int maxStack, int maxLocals) {
                Label finish = new Label();
                super.visitLabel(finish);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                setState(generatorClass.table.getFinishState());
    
                super.visitLabel(generatorClass.table.end);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitInsn(Opcodes.RETURN);

                Label exceptionCleanup = new Label();
                super.visitLabel(exceptionCleanup);
                super.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Throwable"});
                setState(generatorClass.table.getFinishState());
                super.visitInsn(Opcodes.ATHROW);

                super.visitTryCatchBlock(generatorClass.table.start, finish, exceptionCleanup, null);

                // Default case
                super.visitLabel(generatorClass.table.defaultLabel);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
                super.visitInsn(Opcodes.DUP);
                super.visitLdcInsn(GENERATOR_ERR_UNREACHABLE);
                super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
                super.visitInsn(Opcodes.ATHROW);

                super.visitMaxs(maxStack, maxLocals);
            }
        };
        
        return new FrameTracker(Opcodes.ASM7, frame, transformer);
    }

    private GeneratorClass initializeClass(
        Type type,
        String className,
        ClassWriter writer,
        GeneratorTable table
    ) {
        FieldNode stateNode = new FieldNode(Opcodes.ACC_SYNTHETIC, "$state", "I", null, null);
        FieldNode resultNode = new FieldNode(Opcodes.ACC_SYNTHETIC, "$result", type.getDescriptor(), null, null);

        return new GeneratorClass(type, stateNode, resultNode, table);
    }

    private void visitIterator(ClassVisitor classVisitor) {
        MethodVisitor visitor = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            "iterator",
            "()" + Type.getDescriptor(Iterator.class),
            null,
            null
        );
        visitor.visitCode();
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitInsn(Opcodes.ARETURN);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();
    }

    private void visitHasNext(ClassVisitor classVisitor, String className, FieldNode resultNode) {
        MethodVisitor visitor = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            "hasNext",
            "()Z",
            null,
            null
        );
        Label advanceEnd = new Label();
        visitor.visitCode();

        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, className, resultNode.name, resultNode.desc);
        visitor.visitJumpInsn(Opcodes.IFNONNULL, advanceEnd);

        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "advance", "()V", false);

        visitor.visitLabel(advanceEnd);
        visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, className, resultNode.name, resultNode.desc);

        Label hasNextReturnFalse = new Label();
        visitor.visitJumpInsn(Opcodes.IFNULL, hasNextReturnFalse);

        visitor.visitInsn(Opcodes.ICONST_1);
        visitor.visitInsn(Opcodes.IRETURN);

        visitor.visitLabel(hasNextReturnFalse);
        visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        visitor.visitInsn(Opcodes.ICONST_0);
        visitor.visitInsn(Opcodes.IRETURN);

        visitor.visitMaxs(2, 1);
        visitor.visitEnd();
    }
    
    private void visitNext(ClassVisitor classVisitor, String className, FieldNode resultNode) {
        MethodVisitor visitor = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            "next",
            "()Ljava/lang/Object;",
            null,
            null
        );
        visitor.visitCode();
        Label checkEnd = new Label();
        
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, className, resultNode.name, resultNode.desc);
        visitor.visitJumpInsn(Opcodes.IFNONNULL, checkEnd);
        
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "advance", "()V", false);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, className, resultNode.name, resultNode.desc);
        visitor.visitJumpInsn(Opcodes.IFNONNULL, checkEnd);

        String exceptionClass = Type.getInternalName(NoSuchElementException.class);
        visitor.visitTypeInsn(Opcodes.NEW, exceptionClass);
        visitor.visitInsn(Opcodes.DUP);
        visitor.visitLdcInsn(GENERATOR_ERR_RESUME_ON_FINISH);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, exceptionClass, "<init>", "(Ljava/lang/String;)V", false);
        visitor.visitInsn(Opcodes.ATHROW);

        visitor.visitLabel(checkEnd);
        visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, className, resultNode.name, resultNode.desc);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitInsn(Opcodes.ACONST_NULL);
        visitor.visitFieldInsn(Opcodes.PUTFIELD, className, resultNode.name, resultNode.desc);
        visitor.visitInsn(Opcodes.ARETURN);

        visitor.visitMaxs(3, 1);
        visitor.visitEnd();
    }

    private void visitRemove(ClassVisitor classVisitor) {
        MethodVisitor visitor = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            "remove",
            "()V",
            null,
            null
        );
        visitor.visitCode();

        String exceptionClass = Type.getInternalName(UnsupportedOperationException.class);
        visitor.visitTypeInsn(Opcodes.NEW, exceptionClass);
        visitor.visitInsn(Opcodes.DUP);
        visitor.visitLdcInsn("remove");
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, exceptionClass, "<init>", "(Ljava/lang/String;)V", false);
        visitor.visitInsn(Opcodes.ATHROW);

        visitor.visitMaxs(3, 1);
        visitor.visitEnd();
    }

    private GeneratorTable setupTable(ClassReader reader) {
        final String className = reader.getClassName();
        final List<Label> yields = new ArrayList<Label>();

        reader.accept(new ClassVisitor(Opcodes.ASM7) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if ("advance".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM7) {
                        @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            if (isYieldThis(opcode, className, owner, name) || isYieldAll(opcode, className, owner, name)) {
                                yields.add(new Label());
                            }
                        }
                    };
                }

                return null;
            }
        }, ClassReader.SKIP_FRAMES);

        return new GeneratorTable(
            new Label(),
            yields.toArray(new Label[0]),
            new Label(),
            new Label()
        );
    }

    private static class GeneratorClass {
        public final Type type;
        public final FieldNode stateNode;
        public final FieldNode resultNode;
        public final GeneratorTable table;

        public GeneratorClass(Type type, FieldNode stateNode, FieldNode resultNode, GeneratorTable table) {
            this.type = type;
            this.stateNode = stateNode;
            this.resultNode = resultNode;
            this.table = table;
        }
    }

    private static class GeneratorTable {
        public final Label start;
        public final Label[] yields;
        public final Label end;
        public final Label defaultLabel;

        public GeneratorTable(Label start, Label[] yields, Label end, Label defaultLabel) {
            this.start = start;
            this.yields = yields;
            this.end = end;
            this.defaultLabel = defaultLabel;
        }

        public Label[] toLabels() {
            Label[] arr = new Label[yields.length + 2];
            arr[0] = start;
            System.arraycopy(yields, 0, arr, 1, yields.length);
            arr[arr.length - 1] = end;

            return arr;
        }

        public int getFinishState() {
            return yields.length + 1;
        }
    }

    private static class RawInterpreter extends BasicInterpreter {
        public RawInterpreter() {
            super(Opcodes.ASM7);
        }

        @Override public BasicValue newValue(Type type) {
            return new BasicValue(type);
        }
    }

    private static class FrameTracker extends MethodVisitor {
        private final RawInterpreter interpreter;
        public final Frame<BasicValue> frame;

        protected FrameTracker(int api, Frame<BasicValue> frame, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
            this.interpreter = new RawInterpreter();
            this.frame = frame;
        }

        private void updateFrame(AbstractInsnNode node) {
            try {
                frame.execute(node, interpreter);
            } catch (AnalyzerException e) {
                throw new RuntimeException(e);
            }
        }

        private LabelNode[] intoLabelNodes(Label[] labels) {
            LabelNode[] labelNodes = new LabelNode[labels.length];
            for (int i = 0; i < labels.length; i++) {
                labelNodes[i] = new LabelNode(labels[i]);
            }
            return labelNodes;
        }

        @Override public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            updateFrame(new FieldInsnNode(opcode, owner, name, descriptor));
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        private Type[] intoTypes(Object[] frameTypes) {
            List<Type> list = new ArrayList<Type>(frameTypes.length);
            for (Object frameType : frameTypes) {
                if (frameType == null) {
                    break;
                } else if (frameType == Opcodes.TOP) {
                    list.add(null);
                } if (frameType == Opcodes.INTEGER) {
                    list.add(Type.INT_TYPE);
                } else if (frameType == Opcodes.LONG) {
                    list.add(Type.LONG_TYPE);
                } else if (frameType == Opcodes.FLOAT) {
                    list.add(Type.FLOAT_TYPE);
                } else if (frameType == Opcodes.DOUBLE) {
                    list.add(Type.DOUBLE_TYPE);
                } else if (frameType instanceof String) {
                    list.add(Type.getObjectType((String) frameType));
                }
            }

            return list.toArray(new Type[0]);
        }

        private void updateStack(int numStack, Type[] stack) {
            frame.clearStack();
            for (int i = 0; i < numStack; i++) {
                frame.push(interpreter.newValue(stack[i]));
            }
        }

        private void updateLocal(int numLocal, Type[] local) {
            for (int i = 0; i < numLocal; i++) {
                frame.setLocal(i, interpreter.newValue(local[i]));
            }
        }

        @Override public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            switch (type) {
                case Opcodes.F_NEW:
                case Opcodes.F_FULL: {
                    updateStack(numStack, intoTypes(stack));
                    updateLocal(numLocal, intoTypes(local));
                    break;
                }

                case Opcodes.F_SAME1: {
                    frame.clearStack();
                    frame.push(interpreter.newValue(intoTypes(stack)[0]));
                    break;
                }

                case Opcodes.F_CHOP: {
                    frame.clearStack();
                    int start = local.length - 1;

                    for (int i = 0; i < numLocal; i++) {
                        frame.setLocal(start - i, null);
                    }
                    break;
                }
                case Opcodes.F_APPEND: {
                    frame.clearStack();
                    int start = local.length;

                    Type[] localTypes = intoTypes(local);
                    for (int i = 0; i < numLocal; i++) {
                        frame.setLocal(start + i, interpreter.newValue(localTypes[i]));
                    }
                    break;
                }

                case Opcodes.F_SAME: {
                    frame.clearStack();
                    break;
                }

                default: break;
            }

            super.visitFrame(type, numLocal, local, numStack, stack);
        }

        @Override public void visitIincInsn(int varIndex, int increment) {
            updateFrame(new IincInsnNode(varIndex, increment));
            super.visitIincInsn(varIndex, increment);
        }

        @Override public void visitInsn(int opcode) {
            updateFrame(new InsnNode(opcode));
            super.visitInsn(opcode);
        }

        @Override public void visitIntInsn(int opcode, int operand) {
            updateFrame(new IntInsnNode(opcode, operand));
            super.visitIntInsn(opcode, operand);
        }

        @Override public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            updateFrame(new InvokeDynamicInsnNode(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override public void visitJumpInsn(int opcode, Label label) {
            updateFrame(new JumpInsnNode(opcode, new LabelNode(label)));
            super.visitJumpInsn(opcode, label);
        }

        @Override public void visitLdcInsn(Object value) {
            updateFrame(new LdcInsnNode(value));
            super.visitLdcInsn(value);
        }

        @Override public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            updateFrame(new LookupSwitchInsnNode(new LabelNode(dflt), keys, intoLabelNodes(labels)));
            super.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            updateFrame(new MethodInsnNode(opcode, owner, name, descriptor, isInterface));
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            updateFrame(new MultiANewArrayInsnNode(descriptor, numDimensions));
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            updateFrame(new TableSwitchInsnNode(min, max, new LabelNode(dflt), intoLabelNodes(labels)));
            super.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override public void visitTypeInsn(int opcode, String type) {
            updateFrame(new TypeInsnNode(opcode, type));
            super.visitTypeInsn(opcode, type);
        }

        @Override public void visitVarInsn(int opcode, int varIndex) {
            updateFrame(new VarInsnNode(opcode, varIndex));
            super.visitVarInsn(opcode, varIndex);
        }
    }
}
