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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.*;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import lombok.ConfigurationKeys;

import lombok.Versionable;
import lombok.VersionableUtils;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;

import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.apache.log4j.MDC;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;

import org.mangosdk.spi.ProviderFor;

/**
 * 
 * @author https://github.com/lexfaraday
 *
 */
@DeferUntilPostDiet
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleVersionable extends EclipseAnnotationHandler<Versionable> {
    @Override
    public void handle(AnnotationValues<Versionable> annotation, Annotation ast, EclipseNode annotationNode) {
        handleFlagUsage(annotationNode, ConfigurationKeys.VERSIONABLE_FLAG_USAGE, "@Versionable");

        if (annotationNode.up().getKind() != Kind.ARGUMENT)
            return;

        Argument arg;
        AbstractMethodDeclaration declaration;

        try {
            arg = (Argument) annotationNode.up().get();
            declaration = (AbstractMethodDeclaration) annotationNode.up().up().get();
        } catch (Exception e) {
            return;
        }

        if (isGenerated(declaration))
            return;

        if (declaration.isAbstract()) {
            // This used to be a warning
            return;
        }

        // VersionableUtils.resolveVersion(version)
        MessageSend resolveVersionCall = new MessageSend();
        resolveVersionCall.sourceStart = annotationNode.get().sourceStart;
        resolveVersionCall.sourceEnd = annotationNode.get().sourceEnd;
        setGeneratedBy(resolveVersionCall, annotationNode.get());
        resolveVersionCall.receiver = HandleSetterVersionable.generateQualifiedNameRef(annotationNode.get(), "lombok".toCharArray(), VersionableUtils.class.getSimpleName().toCharArray());
        resolveVersionCall.selector = "resolveVersion".toCharArray();
        resolveVersionCall.arguments = new Expression[] { new SingleNameReference(arg.name, 0L) };

        // org.apache.log4j.MDC.put(VersionableUtils.VERSIONABLE_KEY, VersionableUtils.resolveVersion(version));
        MessageSend mdcPut = new MessageSend();
        mdcPut.sourceStart = annotationNode.get().sourceStart;
        mdcPut.sourceEnd = annotationNode.get().sourceEnd;
        setGeneratedBy(mdcPut, annotationNode.get());
        mdcPut.receiver = HandleSetterVersionable.generateQualifiedNameRef(annotationNode.get(), "org".toCharArray(), "apache".toCharArray(), "log4j".toCharArray(), MDC.class.getSimpleName().toCharArray());
        mdcPut.selector = "put".toCharArray();

        mdcPut.arguments = new Expression[] { HandleSetterVersionable.generateQualifiedNameRef(
                                                                                               annotationNode.get(),
                                                                                                   "lombok".toCharArray(),
                                                                                                   VersionableUtils.class.getSimpleName().toCharArray(),
                                                                                                   VersionableUtils.VERSIONABLE_KEY.toCharArray()), resolveVersionCall };
        if (declaration.statements == null) {
            declaration.statements = new Statement[] { mdcPut };
        } else {
            Statement[] newStatements = new Statement[declaration.statements.length + 1];
            int skipOver = 0;
            System.arraycopy(declaration.statements, 0, newStatements, 0, skipOver);
            System.arraycopy(declaration.statements, skipOver, newStatements, skipOver + 1, declaration.statements.length - skipOver);
            newStatements[skipOver] = mdcPut;
            declaration.statements = newStatements;
        }
        annotationNode.up().up().rebuild();
    }
}
