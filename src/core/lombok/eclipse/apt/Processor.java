package lombok.eclipse.apt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import lombok.eclipse.TransformEclipseAST;
import lombok.patcher.inject.LiveInjector;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.util.HashtableOfType;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {
	private BaseProcessingEnvImpl processingEnv;
	
	@Override public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		
		this.processingEnv = (BaseProcessingEnvImpl)procEnv;
		
		new LiveInjector().injectSelf();
	}
	
	/** {@inheritDoc} */
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			Field unitsField = roundEnv.getClass().getDeclaredField("_units");
			unitsField.setAccessible(true);
			CompilationUnitDeclaration[] roots = (CompilationUnitDeclaration[]) unitsField.get(roundEnv);
			
			if (roots == null) System.out.println("roots is null: " + roundEnv.processingOver());
			else System.out.println("rootscount: " + roots.length);
			
			if (roots != null) for (CompilationUnitDeclaration cud : roots) {
				TransformEclipseAST.transform(null, cud);
			}
			
			Field f = processingEnv.getLookupEnvironment().getClass().getDeclaredField("stepCompleted");
			f.setAccessible(true);
			f.set(processingEnv.getLookupEnvironment(), 1);
			if (roots != null) for (CompilationUnitDeclaration cud : roots) {
				Field f2 = cud.scope.fPackage.getClass().getDeclaredField("knownTypes");
				f2.setAccessible(true);
				HashtableOfType turd = (HashtableOfType) f2.get(cud.scope.fPackage);
				int idx = 0;
				for (char[] x : turd.keyTable) {
					if (CharOperation.equals(x, cud.types[0].name)) turd.keyTable[idx] = null;
					idx++;
				}
				Method m = HashtableOfType.class.getDeclaredMethod("rehash");
				m.setAccessible(true);
				m.invoke(turd);
				cud.scope = null;
				cud.types[0].binding = null;
				cud.types[0].scope = null;
				processingEnv.getLookupEnvironment().buildTypeBindings(cud, null);
			}
			
			processingEnv.getLookupEnvironment().completeTypeBindings();
			return false;
		} catch (Throwable t) {
			System.out.println("Scream and shout!");
			t.printStackTrace();
			return false;
		}
	}
}
