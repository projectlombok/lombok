package lombok.eclipse;

import static lombok.eclipse.Eclipse.toQualifiedName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import lombok.core.AnnotationValues;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.core.AnnotationValues.AnnotationValueDecodeFail;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class HandlerLibrary {
	private TypeLibrary typeLibrary = new TypeLibrary();
	
	private static class AnnotationHandlerContainer<T extends Annotation> {
		private EclipseAnnotationHandler<T> handler;
		private Class<T> annotationClass;
		
		AnnotationHandlerContainer(EclipseAnnotationHandler<T> handler, Class<T> annotationClass) {
			this.handler = handler;
			this.annotationClass = annotationClass;
		}
		
		public boolean handle(org.eclipse.jdt.internal.compiler.ast.Annotation annotation,
				final Node annotationNode) {
			Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
			
			final MemberValuePair[] pairs = annotation.memberValuePairs();
			for ( Method m : annotationClass.getDeclaredMethods() ) {
				if ( !Modifier.isPublic(m.getModifiers()) ) continue;
				String name = m.getName();
				List<String> raws = new ArrayList<String>();
				List<Object> guesses = new ArrayList<Object>();
				Expression fullExpression = null;
				Expression[] expressions = null;
				
				if ( pairs != null ) for ( MemberValuePair pair : pairs ) {
					char[] n = pair.name;
					String mName = n == null ? "value" : new String(name);
					if ( !mName.equals(name) ) continue;
					fullExpression = pair.value;
				}
				
				if ( fullExpression != null ) {
					if ( fullExpression instanceof ArrayInitializer ) {
						expressions = ((ArrayInitializer)fullExpression).expressions;
					} else expressions = new Expression[] { fullExpression };
					for ( Expression ex : expressions ) {
						StringBuffer sb = new StringBuffer();
						ex.print(0, sb);
						raws.add(sb.toString());
						guesses.add(calculateValue(ex));
					}
				}
				
				final Expression fullExpr = fullExpression;
				final Expression[] exprs = expressions;
				
				values.put(name, new AnnotationValue(annotationNode, raws, guesses) {
					@Override public void setError(String message, int valueIdx) {
						Expression ex;
						if ( valueIdx == -1 ) ex = fullExpr;
						else ex = exprs[valueIdx];
						
						int sourceStart = ex.sourceStart;
						int sourceEnd = ex.sourceEnd;
						
						annotationNode.addError(message, sourceStart, sourceEnd);
					}
				});
			}
			
			return handler.handle(new AnnotationValues<T>(annotationClass, values, annotationNode), annotation, annotationNode);
		}
	}
	
	private Map<String, AnnotationHandlerContainer<?>> annotationHandlers =
		new HashMap<String, AnnotationHandlerContainer<?>>();
	
	private Collection<EclipseASTVisitor> visitorHandlers = new ArrayList<EclipseASTVisitor>();
	
	private static Object calculateValue(Expression e) {
		if ( e instanceof Literal ) {
			((Literal)e).computeConstant();
			switch ( e.constant.typeID() ) {
			case TypeIds.T_int: return e.constant.intValue();
			case TypeIds.T_byte: return e.constant.byteValue();
			case TypeIds.T_short: return e.constant.shortValue();
			case TypeIds.T_char: return e.constant.charValue();
			case TypeIds.T_float: return e.constant.floatValue();
			case TypeIds.T_double: return e.constant.doubleValue();
			case TypeIds.T_boolean: return e.constant.booleanValue();
			case TypeIds.T_long: return e.constant.longValue();
			case TypeIds.T_JavaLangString: return e.constant.stringValue();
			default: return null;
			}
		} else if ( e instanceof ClassLiteralAccess ) {
			return Eclipse.toQualifiedName(((ClassLiteralAccess)e).type.getTypeName());
		} else if ( e instanceof SingleNameReference ) {
			return new String(((SingleNameReference)e).token);
		} else if ( e instanceof QualifiedNameReference ) {
			String qName = Eclipse.toQualifiedName(((QualifiedNameReference)e).tokens);
			int idx = qName.lastIndexOf('.');
			return idx == -1 ? qName : qName.substring(idx+1);
		}
		
		return null;
	}
	
	public static HandlerLibrary load() {
		HandlerLibrary lib = new HandlerLibrary();
		
		loadAnnotationHandlers(lib);
		loadVisitorHandlers(lib);
		
		return lib;
	}
	
	@SuppressWarnings("unchecked") private static void loadAnnotationHandlers(HandlerLibrary lib) {
		Iterator<EclipseAnnotationHandler> it = ServiceLoader.load(EclipseAnnotationHandler.class).iterator();
		while ( it.hasNext() ) {
			try {
				EclipseAnnotationHandler<?> handler = it.next();
				Class<? extends Annotation> annotationClass =
					SpiLoadUtil.findAnnotationClass(handler.getClass(), EclipseAnnotationHandler.class);
				AnnotationHandlerContainer<?> container = new AnnotationHandlerContainer(handler, annotationClass);
				if ( lib.annotationHandlers.put(container.annotationClass.getName(), container) != null ) {
					Eclipse.error("Duplicate handlers for annotation type: " + container.annotationClass.getName());
				}
				lib.typeLibrary.addType(container.annotationClass.getName());
			} catch ( ServiceConfigurationError e ) {
				Eclipse.error("Can't load Lombok annotation handler for eclipse: ", e);
			}
		}
	}
	
	private static void loadVisitorHandlers(HandlerLibrary lib) {
		Iterator<EclipseASTVisitor> it = ServiceLoader.load(EclipseASTVisitor.class).iterator();
		while ( it.hasNext() ) {
			try {
				lib.visitorHandlers.add(it.next());
			} catch ( ServiceConfigurationError e ) {
				Eclipse.error("Can't load Lombok visitor handler for eclipse: ", e);
			}
		}
	}
	
	public boolean handle(CompilationUnitDeclaration ast, EclipseAST.Node annotationNode,
			org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		String pkgName = annotationNode.getPackageDeclaration();
		Collection<String> imports = annotationNode.getImportStatements();
		
		TypeResolver resolver = new TypeResolver(typeLibrary, pkgName, imports);
		TypeReference rawType = annotation.type;
		if ( rawType == null ) return false;
		boolean handled = false;
		for ( String fqn : resolver.findTypeMatches(annotationNode, toQualifiedName(annotation.type.getTypeName())) ) {
			AnnotationHandlerContainer<?> container = annotationHandlers.get(fqn);
			if ( container == null ) continue;
			
			try {
				handled |= container.handle(annotation, annotationNode);
			} catch ( AnnotationValueDecodeFail fail ) {
				fail.owner.setError(fail.getMessage(), fail.idx);
			} catch ( Throwable t ) {
				Eclipse.error(String.format("Lombok annotation handler %s failed", container.handler.getClass()), t);
			}
		}
		
		return handled;
	}
	
	public void callASTVisitors(EclipseAST ast) {
		for ( EclipseASTVisitor visitor : visitorHandlers ) try {
			ast.traverse(visitor);
		} catch ( Throwable t ) {
			Eclipse.error(String.format("Lombok visitor handler %s failed", visitor.getClass()), t);
		}
	}
}
