package lombok.eclipse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.core.AnnotationValues;
import lombok.core.TypeLibrary;
import lombok.core.TypeResolver;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.osgi.framework.Bundle;

public class Eclipse {
	private Eclipse() {
		//Prevent instantiation
	}
	
	private static final String DEFAULT_BUNDLE = "org.eclipse.jdt.core";
	public static void error(String message) {
		error(message, DEFAULT_BUNDLE, null);
	}
	
	public static void error(String message, Throwable error) {
		error(message, DEFAULT_BUNDLE, error);
	}
	
	public static void error(String message, String bundleName) {
		error(message, bundleName, null);
	}
	
	public static void error(String message, String bundleName, Throwable error) {
		Bundle bundle = Platform.getBundle(bundleName);
		if ( bundle == null ) {
			System.err.printf("Can't find bundle %s while trying to report error:\n%s\n", bundleName, message);
			return;
		}
		
		ILog log = Platform.getLog(bundle);
		
		log.log(new Status(IStatus.ERROR, bundleName, message, error));
	}
	
	static String toQualifiedName(char[][] typeName) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for ( char[] c : typeName ) {
			sb.append(first ? "" : ".").append(c);
			first = false;
		}
		return sb.toString();
	}
	
	public static boolean annotationTypeMatches(Class<? extends java.lang.annotation.Annotation> type, Node node) {
		if ( node.getKind() != Kind.ANNOTATION ) return false;
		TypeReference typeRef = ((Annotation)node.get()).type;
		if ( typeRef == null || typeRef.getTypeName() == null ) return false;
		String typeName = toQualifiedName(typeRef.getTypeName());
		
		TypeLibrary library = new TypeLibrary();
		library.addType(type.getName());
		TypeResolver resolver = new TypeResolver(library, node.getPackageDeclaration(), node.getImportStatements());
		Collection<String> typeMatches = resolver.findTypeMatches(node, typeName);
		
		for ( String match : typeMatches ) {
			if ( match.equals(type.getName()) ) return true;
		}
		
		return false;
	}
	
	public static <A extends java.lang.annotation.Annotation> AnnotationValues<A>
			createAnnotation(Class<A> type, final Node annotationNode) {
		Annotation annotation = (Annotation) annotationNode.get();
		Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
		
		final MemberValuePair[] pairs = annotation.memberValuePairs();
		for ( Method m : type.getDeclaredMethods() ) {
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
		
		return new AnnotationValues<A>(type, values, annotationNode);
	}
	
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
}
