package lombok.mapstruct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.CodeSource;

import javax.lang.model.type.TypeMirror;

import org.mapstruct.ap.spi.AstModifyingAnnotationProcessor;

class NotifierHider {
	public static class AstModificationNotifier implements AstModifyingAnnotationProcessor {
		private static Class<?> data;
		private static Method isTypeComplete;
		private static IllegalStateException failure;
		
		@Override public boolean isTypeComplete(TypeMirror type) {
			if (System.getProperty("lombok.disable") != null) return true;
			// Under ecj lombok runs as an agent and is usually not on the processor path.
			if (!type.getClass().getName().startsWith("com.sun.tools.javac.")) return true;
			if (failure != null) throw failure;
			
			try {
				if (data == null) data = Class.forName("lombok.launch.AnnotationProcessorHider$AstModificationNotifierData");
				if (isTypeComplete == null) isTypeComplete = data.getMethod("isTypeComplete", TypeMirror.class);
				return (Boolean) isTypeComplete.invoke(null, type);
			} catch (Exception e) {
				failure = explain(e instanceof InvocationTargetException ? e.getCause() : e);
				throw failure;
			}
		}

		private static IllegalStateException explain(Throwable cause) {
			String message = "lombok-mapstruct-binding cannot ask lombok whether a type is complete: " + cause
				+ ". Either lombok and lombok-mapstruct-binding versions do not match, or lombok is missing from the annotation processor path. This binding needs lombok 1.18.50 or newer.";
			CodeSource source = data == null ? null : data.getProtectionDomain().getCodeSource();
			if (source != null) message += " Found lombok at " + source.getLocation() + ".";
			return new IllegalStateException(message, cause);
		}
	}
}
