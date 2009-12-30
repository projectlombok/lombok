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
package lombok.eclipse.apt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import lombok.Lombok;
import lombok.eclipse.TransformEclipseAST;
import lombok.eclipse.agent.PatchFixes;
import lombok.patcher.inject.LiveInjector;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.util.HashtableOfType;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {
	private BaseProcessingEnvImpl processingEnv;
	
	@Override public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		
		this.processingEnv = (BaseProcessingEnvImpl)procEnv;
		
		if (eclipseNeedsPatching()) {
			new LiveInjector().injectSelf();
		}
	}
	
	private boolean eclipseNeedsPatching() {
		try {
			return Class.forName("org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration").getDeclaredField("$lombokAST") == null;
		} catch (Exception ignore) {
			//I guess it isn't there, then.
			return true;
		}
	}
	
	/** {@inheritDoc} */
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		CompilationUnitDeclaration[] roots = (CompilationUnitDeclaration[]) fieldAccess(roundEnv, "_units");
		
		if (roots == null) return false;
		for (CompilationUnitDeclaration cud : roots) {
			//ECJ, like eclipse, first 'diet parses' - parsing only structure and not method bodies, and then later parses method bodies.
			//On eclipse, we instrument both runs, but on ECJ, instrumenting is rather limited, and unless you get major errors or use -proc:none,
			//full parsing is inevitable, so we just get it over with up front, so we can lombokize everything in one go instead of in 2 phases.
			fullParseType(processingEnv.getCompiler().parser, cud, cud.types);
			TransformEclipseAST.transform(null, cud);
		}
		
		Map<PackageBinding, List<char[]>> packageToTypeNames = new HashMap<PackageBinding, List<char[]>>();
		
		for (CompilationUnitDeclaration cud : roots) {
			addToClearTracker(packageToTypeNames, new char[0], cud.scope.fPackage, cud.types);
		}
		
		for (Map.Entry<PackageBinding, List<char[]>> e : packageToTypeNames.entrySet()) {
			if (e.getValue().isEmpty()) continue;
			HashtableOfType knownTypes = (HashtableOfType) fieldAccess(e.getKey(), "knownTypes");
			for (int i = 0; i < knownTypes.keyTable.length; i++) {
				if (knownTypes.keyTable[i] == null) continue;
				for (char[] toClear : e.getValue()) {
					if (CharOperation.equals(knownTypes.keyTable[i], toClear)) knownTypes.keyTable[i] = null;
				}
			}
			
			methodInvoke(knownTypes, "rehash");
		}
		
		for (CompilationUnitDeclaration cud : roots) {
			cud.scope = null;
			cud.types[0].binding = null;
			cud.types[0].scope = null;
			processingEnv.getLookupEnvironment().buildTypeBindings(cud, null);
		}
		
		processingEnv.getLookupEnvironment().completeTypeBindings();
		return false;
	}
	
	private static void fullParseType(Parser parser, CompilationUnitDeclaration cud, TypeDeclaration[] typeDecls) {
		if (typeDecls == null) return;
		for (TypeDeclaration typeDecl : typeDecls) {
			for (AbstractMethodDeclaration methodDecl : typeDecl.methods) {
				methodDecl.parseStatements(parser, cud);
				methodDecl.bits |= PatchFixes.ALREADY_PROCESSED_FLAG;
			}
			fullParseType(parser, cud, typeDecl.memberTypes);
		}
	}
	
	private static void addToClearTracker(Map<PackageBinding, List<char[]>> packageToTypeNames, char[] prefix, PackageBinding packageBinding, TypeDeclaration[] typeDecls) {
		if (typeDecls == null) return;
		List<char[]> names = packageToTypeNames.get(packageBinding);
		if (names == null) {
			names = new ArrayList<char[]>();
			packageToTypeNames.put(packageBinding, names);
		}
		for (TypeDeclaration typeDecl : typeDecls) {
			char[] name;
			if (prefix.length == 0) name = typeDecl.name;
			else {
				name = Arrays.copyOf(prefix, prefix.length + typeDecl.name.length);
				System.arraycopy(typeDecl.name, 0, name, prefix.length, typeDecl.name.length);
			}
			names.add(name);
			if (typeDecl.memberTypes != null && typeDecl.memberTypes.length > 0) {
				char[] newPrefix = Arrays.copyOf(name, name.length +1);
				newPrefix[newPrefix.length-1] = '$';
				addToClearTracker(packageToTypeNames, newPrefix, packageBinding, typeDecl.memberTypes);
			}
		}
	}
	
	private static void methodInvoke(Object object, String methodName) {
		try {
			Method m = object.getClass().getDeclaredMethod(methodName);
			m.setAccessible(true);
			m.invoke(object);
		} catch (Exception e) {
			throw Lombok.sneakyThrow(e);
		}
	}
	
	private static Object fieldAccess(Object object, String fieldName) {
		try {
			Field f = object.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(object);
		} catch (Exception e) {
			throw Lombok.sneakyThrow(e);
		}
	}
}
