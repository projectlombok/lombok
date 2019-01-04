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

import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import lombok.core.HandlerPriority;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.handlers.JavacHandlerUtil.CopyJavadoc;
import org.mangosdk.spi.ProviderFor;


/**
 * Replaces the javadoc on each field with itself run through each copy mode, keeping the "remaining" part in each step.
 * In particular, this removes @return and @param tags as well as GETTER/SETTER/WITHER sections.
 */
@ProviderFor(JavacASTVisitor.class)
@HandlerPriority(268435456) // 2^28; run this as late as possible but before HandlePrintAST which is at 2^29.
public class HandleCleanupFieldJavadoc extends JavacASTAdapter {

    @Override
    public void endVisitField(JavacNode fieldNode, JCVariableDecl field) {
        final String originalJavadoc = JavacHandlerUtil.getJavadoc(fieldNode);

        if (originalJavadoc != null) {
            String reduced = originalJavadoc;

            for (CopyJavadoc copyMode : CopyJavadoc.values()) {
                reduced = JavacHandlerUtil.filterJavadocString(fieldNode, copyMode, reduced)[1];
            }

            JavacHandlerUtil.putJavadoc(fieldNode, reduced);
        }
    }

}
