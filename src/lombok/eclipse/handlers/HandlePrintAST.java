/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.Lombok;
import lombok.core.AnnotationValues;
import lombok.core.PrintAST;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

/**
 * Handles the <code>lombok.core.PrintAST</code> annotation for javac.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandlePrintAST implements EclipseAnnotationHandler<PrintAST> {
	public boolean handle(AnnotationValues<PrintAST> annotation, Annotation ast, Node annotationNode) {
		if ( !annotationNode.isCompleteParse() ) return false;
		
		PrintStream stream = System.out;
		String fileName = annotation.getInstance().outfile();
		if ( fileName.length() > 0 ) try {
			stream = new PrintStream(new File(fileName));
		} catch ( FileNotFoundException e ) {
			Lombok.sneakyThrow(e);
		}
		
		annotationNode.up().traverse(new EclipseASTVisitor.Printer(annotation.getInstance().printContent(), stream));
		return true;
	}
}
