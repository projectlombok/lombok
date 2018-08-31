/*
 * Copyright (C) 2010-2018 The Project Lombok Authors.
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
package lombok.javac.handlers;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import lombok.BindTo;
import lombok.bind;
import lombok.core.HandlerPriority;
import lombok.javac.*;
import org.mangosdk.spi.ProviderFor;

import static lombok.javac.handlers.JavacHandlerUtil.*;

@ProviderFor(JavacASTVisitor.class)
@HandlerPriority(65530)
@ResolutionResetNeeded
public class HandleBind extends JavacASTAdapter {

    private static boolean eq(String typeTreeToString, String key) {
        return typeTreeToString.equals(key) || typeTreeToString.equals("lombok." + key) || typeTreeToString.equals("lombok.experimental." + key);
    }

    @Override
    public void visitLocal(JavacNode localNode, JCVariableDecl local) {
        try {
            JCTree typeTree = local.vartype;
            if (typeTree == null) return;

            if (!localNode.hasAnnotation(bind.class)) return;

            JavacNode parent = localNode.directUp();
            JCTree parentt = parent.get();

            final List<JCStatement> oldSt;
            if (parentt instanceof JCBlock)
                oldSt = ((JCBlock) parentt).stats;
            else if (parentt instanceof JCMethodDecl)
                oldSt = ((JCMethodDecl) parentt).body.stats;
            else
                throw new IllegalStateException("the bind container must be block or method declaration");

            if (!(oldSt.last() instanceof JCReturn))
                throw new IllegalStateException("the last statement must be return");

            // split => local == oldSt[i]
            final int partition;
            {
                int i = 0;
                while (i < oldSt.size() && oldSt.get(i) != local)
                    i += 1;
                if (i >= oldSt.size())
                    throw new IllegalStateException("bug 52f2f45a-b2ee-4152-8313-8645cb48c07a");
                partition = i;
            }

            final JCStatement[] newBody = new JCStatement[oldSt.size() - partition - 1];

            for (int i = partition + 1; i < oldSt.size(); i++)
                newBody[i - partition - 1] = oldSt.get(i);

            if (!(local.init instanceof JCMethodInvocation))
                throw new IllegalStateException("method invocation expected");

            final JCMethodInvocation methodI = (JCMethodInvocation) local.init;

            if (!(methodI.getMethodSelect() instanceof JCFieldAccess))
                throw new IllegalStateException("field access expected");

            final JCFieldAccess fieldA = (JCFieldAccess) methodI.getMethodSelect();

            final String fake = fieldA.name.toString();

            // lookup bind operation
            final Type monadType = new JavacResolution(localNode.getContext()).resolveMethodMember(localNode).get(((JCFieldAccess) methodI.meth).selected).type;
            if (!(monadType instanceof Type.ClassType))
                throw new IllegalStateException("expected to be a monad class");
            final Type.ClassType monadClass = (Type.ClassType) monadType;

            Symbol.MethodSymbol bindMethod = null;
            for (final Symbol s : monadClass.tsym.getEnclosedElements()) {
                if (s instanceof Symbol.MethodSymbol) {
                    final Symbol.MethodSymbol ms = (Symbol.MethodSymbol) s;
                    if (fake.equals(ms.name.toString())) {
                        bindMethod = ms;
                        break;
                    }
                }
            }
            if (bindMethod == null)
                throw new IllegalStateException("bind method expected to be into monad class");

            final BindTo bindTo = bindMethod.getAnnotation(BindTo.class);
            if (bindTo == null)
                throw new IllegalStateException("bind method expected to have the @BindTo annotation");

            // monadic expression to expand
            final JCExpression k = fieldA.selected;

            final JCStatement[] newSt = new JCStatement[partition + 1];

            // copy preceding
            for (int i = 0; i < partition; i++)
                newSt[i] = oldSt.get(i);

            final JavacTreeMaker maker = localNode.getAst().getTreeMaker();

            // make return
            final JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(localNode, "Override"), List.<JCExpression>nil());
            final JCReturn rexp;
            {
                final JCFieldAccess bindOp = maker.Select(k, localNode.toName(bindTo.method()));
                final List<JCExpression> lambda = List.<JCExpression>of(maker.NewClass(
                        null,
                        List.<JCExpression>nil(),
                        maker.TypeApply(chainDots(localNode, "java", "util", "function", "Function"), List.of(local.vartype, ((JCMethodDecl) parentt).restype)),
                        List.<JCExpression>nil(),
                        maker.ClassDef(
                                maker.Modifiers(0),
                                localNode.toName(""),
                                List.<JCTypeParameter>nil(),
                                null,
                                List.<JCExpression>nil(),
                                List.<JCTree>of(
                                        recursiveSetGeneratedBy(
                                                maker.MethodDef(
                                                        maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation)),
                                                        localNode.toName("apply"),
                                                        ((JCMethodDecl) parentt).restype,
                                                        List.<JCTypeParameter>nil(),
                                                        List.<JCVariableDecl>of(maker.VarDef(/* oldVar.mods */ maker.Modifiers(Flags.PARAMETER), local.name, local.vartype, null)),
                                                        List.<JCExpression>nil(),
                                                        maker.Block(0, List.from(newBody)),
                                                        null
                                                ),
                                                localNode.get(),
                                                localNode.getContext()
                                        )
                                )
                        )
                ));
                final JCMethodInvocation appe = maker.Apply(List.<JCExpression>nil(), bindOp, lambda);
                rexp = maker.Return(appe);
            }

            newSt[partition] = rexp;

            if (parentt instanceof JCBlock)
                ((JCBlock) parentt).stats = List.from(newSt);
            else if (parentt instanceof JCMethodDecl)
                ((JCMethodDecl) parentt).body.stats = List.from(newSt);
            else
                throw new IllegalStateException("bug 1d904f35-9285-4082-8f09-c8f293d517d6");

            parent.rebuild();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
