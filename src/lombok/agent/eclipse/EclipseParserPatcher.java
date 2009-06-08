package lombok.agent.eclipse;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

public class EclipseParserPatcher {
	private static class Patcher implements ClassFileTransformer {
		@Override public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {
			
			if ( !ECLIPSE_PARSER_CLASS_NAME.equals(className) ) return null;
			EclipseParserTransformer transformer = new EclipseParserTransformer(classfileBuffer);
			return transformer.transform();
		}
	}

	static final String ECLIPSE_PARSER_CLASS_NAME = "org/eclipse/jdt/internal/compiler/parser/Parser";
	
	public static void agentmain(String agentArgs, Instrumentation instrumentation) {
		registerPatcher(instrumentation, true);
	}
	
	public static void premain(String agentArgs, Instrumentation instrumentation) {
		registerPatcher(instrumentation, false);
	}
	
	private static void registerPatcher(Instrumentation instrumentation, boolean transformExisting) {
		instrumentation.addTransformer(new Patcher(), true);
		
		if ( transformExisting ) for ( Class<?> c : instrumentation.getAllLoadedClasses() ) {
			if ( c.getName().equals(ECLIPSE_PARSER_CLASS_NAME) ) {
				try {
					instrumentation.retransformClasses(c);
				} catch ( UnmodifiableClassException ex ) {
					throw new UnsupportedOperationException(
							"The eclipse parser class is already loaded and cannot be modified. " +
							"You'll have to restart eclipse in order to use Lombok in eclipse.");
				}
			}
		}
	}
}
