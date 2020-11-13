package lombok.eclipse.agent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.Lombok;

public class PatchExtensionMethodPortal {
	private static final String TYPE_BINDING = "org.eclipse.jdt.internal.compiler.lookup.TypeBinding";
	private static final String TYPE_BINDING_ARRAY = "[Lorg.eclipse.jdt.internal.compiler.lookup.TypeBinding;";
	private static final String MESSAGE_SEND = "org.eclipse.jdt.internal.compiler.ast.MessageSend";
	private static final String BLOCK_SCOPE = "org.eclipse.jdt.internal.compiler.lookup.BlockScope";
	private static final String METHOD_BINDING = "org.eclipse.jdt.internal.compiler.lookup.MethodBinding";
	private static final String PROBLEM_REPORTER = "org.eclipse.jdt.internal.compiler.problem.ProblemReporter";

	public static Object resolveType(Object resolvedType, Object methodCall, Object scope) {
		try {
			return Reflection.resolveType.invoke(null, resolvedType, methodCall, scope);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return resolvedType;
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e);
		} catch (NullPointerException e) {
			if (!"false".equals(System.getProperty("lombok.debug.reflection", "false"))) {
				e.initCause(Reflection.problem);
				throw e;
			}
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
			return resolvedType;
		}
	}

	public static void errorNoMethodFor(Object problemReporter, Object messageSend, Object recType, Object params) {
		try {
			Reflection.errorNoMethodFor.invoke(null, problemReporter, messageSend, recType, params);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e.getCause());
		} catch (NullPointerException e) {
			if (!"false".equals(System.getProperty("lombok.debug.reflection", "false"))) {
				e.initCause(Reflection.problem);
				throw e;
			}
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
		}
	}
	
	public static void invalidMethod(Object problemReporter, Object messageSend, Object method) {
		try {
			Reflection.invalidMethod.invoke(null, problemReporter, messageSend, method);
		} catch (NoClassDefFoundError e) {
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
		} catch (IllegalAccessException e) {
			handleReflectionDebug(e);
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			handleReflectionDebug(e.getCause());
			throw Lombok.sneakyThrow(e.getCause());
		} catch (NullPointerException e) {
			handleReflectionDebug(e);
			//ignore, we don't have access to the correct ECJ classes, so lombok can't possibly
			//do anything useful here.
		}
	}
	
	public static boolean isDebugReflection() {
		return !"false".equals(System.getProperty("lombok.debug.reflection", "false"));
	}
	
	public static void handleReflectionDebug(Throwable t) {
		if (!isDebugReflection()) return;
		
		System.err.println("** LOMBOK REFLECTION exception: " + t.getClass() + ": " + (t.getMessage() == null ? "(no message)" : t.getMessage()));
		t.printStackTrace(System.err);
		if (Reflection.problem != null) {
			System.err.println("*** ADDITIONALLY, exception occurred setting up reflection: ");
			Reflection.problem.printStackTrace(System.err);
		}
	}
	
	private static final class Reflection {
		public static final Method resolveType, errorNoMethodFor, invalidMethod;
		public static final Throwable problem;
		
		static {
			Method m = null, n = null, o = null;
			Throwable problem_ = null;
			try {
				m = PatchExtensionMethod.class.getMethod("resolveType", Object.class, Class.forName(MESSAGE_SEND), Class.forName(BLOCK_SCOPE));
				n = PatchExtensionMethod.class.getMethod("errorNoMethodFor", Class.forName(PROBLEM_REPORTER),
						Class.forName(MESSAGE_SEND), Class.forName(TYPE_BINDING), Class.forName(TYPE_BINDING_ARRAY));
				o = PatchExtensionMethod.class.getMethod("invalidMethod", Class.forName(PROBLEM_REPORTER), Class.forName(MESSAGE_SEND), Class.forName(METHOD_BINDING));
			} catch (Throwable t) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
				problem_ = t;
			}
			resolveType = m;
			errorNoMethodFor = n;
			invalidMethod = o;
			problem = problem_;
		}
	}
}
