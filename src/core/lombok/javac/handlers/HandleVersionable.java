/*
 * Copyright (C) 2013-2014 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.apache.log4j.MDC;
import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.NonNull;
import lombok.Versionable;
import lombok.Version;
import lombok.VersionableUtils;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import static lombok.javac.JavacTreeMaker.TypeTag.*;
import static lombok.javac.JavacTreeMaker.TreeTag.*;

/**
 * 
 * @author https://github.com/lexfaraday
 *
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleVersionable extends JavacAnnotationHandler<Versionable> {

    @Override
    public void handle(AnnotationValues<Versionable> annotation, JCAnnotation ast, JavacNode annotationNode) {
        handleFlagUsage(annotationNode, ConfigurationKeys.VERSIONABLE_FLAG_USAGE, "@Versionable");

        if (annotationNode.up().getKind() != Kind.ARGUMENT)
            return;

        JCMethodDecl declaration;

        try {
            declaration = (JCMethodDecl) annotationNode.up().up().get();
        } catch (Exception e) {
            return;
        }

        if (declaration.body == null) {
            // This used to be a warning
            return;
        }

        JavacTreeMaker treeMaker = annotationNode.getTreeMaker();
        JCVariableDecl varDecl = (JCVariableDecl) annotationNode.up().get();
        Name fieldName = varDecl.name;

        // VersionableUtils.resolveVersion(version)
        JCExpression currentVersion = JavacHandlerUtil.chainDots(annotationNode.up(), "lombok", VersionableUtils.class.getSimpleName());
        JCExpression resolveVersion = treeMaker.Apply(List.<JCExpression> nil(), treeMaker.Select(currentVersion, annotationNode.up().toName("resolveVersion")), List.<JCExpression> of(treeMaker.Ident(fieldName)));

        // org.apache.log4j.MDC.put(VersionableUtils.VERSIONABLE_KEY, VersionableUtils.resolveVersion(version));
        JCExpression orgApacheLog4jMDCPut = JavacHandlerUtil.chainDots(annotationNode.up(), "org", "apache", "log4j", MDC.class.getSimpleName());
        ListBuffer<JCExpression> putArguments = new ListBuffer<JCExpression>();
        putArguments.add(JavacHandlerUtil.chainDots(annotationNode.up(), "lombok", VersionableUtils.class.getSimpleName(), VersionableUtils.VERSIONABLE_KEY));
        putArguments.add(resolveVersion);
        JCExpression mdcPutVersion = treeMaker.Apply(List.<JCExpression> nil(), treeMaker.Select(orgApacheLog4jMDCPut, annotationNode.up().toName("put")), putArguments.toList());

        List<JCStatement> statements = declaration.body.stats;
        List<JCStatement> tail = statements;
        List<JCStatement> head = List.nil();
        List<JCStatement> newList = tail.prepend(treeMaker.Exec(mdcPutVersion));
        for (JCStatement stat : head)
            newList = newList.prepend(stat);
        declaration.body.stats = newList;
        annotationNode.getAst().setChanged();
    }

}
