package lombok.javac;

import java.lang.annotation.Annotation;
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
import lombok.javac.JavacAST.Node;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

public class Javac {
	private Javac() {
		//prevent instantiation
	}
	
	public static boolean annotationTypeMatches(Class<? extends Annotation> type, Node node) {
		if ( node.getKind() != Kind.ANNOTATION ) return false;
		String typeName = ((JCAnnotation)node.get()).annotationType.toString();
		
		TypeLibrary library = new TypeLibrary();
		library.addType(type.getName());
		TypeResolver resolver = new TypeResolver(library, node.getPackageDeclaration(), node.getImportStatements());
		Collection<String> typeMatches = resolver.findTypeMatches(node, typeName);
		
		for ( String match : typeMatches ) {
			if ( match.equals(type.getName()) ) return true;
		}
		
		return false;
	}
	
	public static <A extends Annotation> AnnotationValues<A> createAnnotation(Class<A> type, final Node node) {
		Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();
		JCAnnotation anno = (JCAnnotation) node.get();
		List<JCExpression> arguments = anno.getArguments();
		for ( Method m : type.getDeclaredMethods() ) {
			if ( !Modifier.isPublic(m.getModifiers()) ) continue;
			String name = m.getName();
			List<String> raws = new ArrayList<String>();
			List<Object> guesses = new ArrayList<Object>();
			final List<DiagnosticPosition> positions = new ArrayList<DiagnosticPosition>();
			
			for ( JCExpression arg : arguments ) {
				JCAssign assign = (JCAssign) arg;
				String mName = assign.lhs.toString();
				if ( !mName.equals(name) ) continue;
				JCExpression rhs = assign.rhs;
				if ( rhs instanceof JCNewArray ) {
					List<JCExpression> elems = ((JCNewArray)rhs).elems;
					for  ( JCExpression inner : elems ) {
						raws.add(inner.toString());
						guesses.add(calculateGuess(inner));
						positions.add(inner.pos());
					}
				} else {
					raws.add(rhs.toString());
					guesses.add(calculateGuess(rhs));
					positions.add(rhs.pos());
				}
			}
			
			values.put(name, new AnnotationValue(node, raws, guesses) {
				@Override public void setError(String message, int valueIdx) {
					node.addError(message, positions.get(valueIdx));
				}
			});
		}
		
		return new AnnotationValues<A>(type, values, node);
	}
	
	private static Object calculateGuess(JCExpression expr) {
		if ( expr instanceof JCLiteral ) {
			JCLiteral lit = (JCLiteral)expr;
			if ( lit.getKind() == com.sun.source.tree.Tree.Kind.BOOLEAN_LITERAL ) {
				return ((Number)lit.value).intValue() == 0 ? false : true;
			}
			return lit.value;
		} else if ( expr instanceof JCIdent || expr instanceof JCFieldAccess ) {
			String x = expr.toString();
			if ( x.endsWith(".class") ) x = x.substring(0, x.length() - 6);
			else {
				int idx = x.lastIndexOf('.');
				if ( idx > -1 ) x = x.substring(idx + 1);
			}
			return x;
		} else return null;
	}
}
