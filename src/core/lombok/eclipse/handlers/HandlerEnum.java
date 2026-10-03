package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.Enum;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;

@ProviderFor(EclipseAnnotationHandler.class) public class HandlerEnum extends EclipseAnnotationHandler<lombok.Enum> {
	
	@Override public void handle(AnnotationValues<Enum> annotation, Annotation ast, EclipseNode annotationNode) {
		Generator generator = new Generator(annotationNode.up(), annotationNode);
		generator.generate();
	}
	
	private class Generator {
		private EclipseNode classNode;
		private EclipseNode annotationNode;
		
		public Generator(EclipseNode classNode, EclipseNode annotationNode) {
			this.classNode = classNode;
			this.annotationNode = annotationNode;
		}
		
		public void generate() {
			addPrivateAllArgsConstructor();
			for (EclipseNode field : findAllFields()) {
				addFinderMethodForField(field);
				makeFieldFinal(field);
			}
			makeEnumPublic();
		}
		
		private void addPrivateAllArgsConstructor() {
			new HandleConstructor().generateAllArgsConstructor(classNode, AccessLevel.PRIVATE, null, SkipIfConstructorExists.YES, Collections.<Annotation>emptyList(), annotationNode);
		}
		
		private java.util.List<EclipseNode> findAllFields() {
			java.util.List<EclipseNode> fields = new LinkedList<EclipseNode>();
			for (EclipseNode field : classNode.down()) {
				if (field.getKind() != Kind.FIELD) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
				if (filterField(fieldDecl)) {
					fields.add(field);
				}
			}
			return fields;
		}
		
		private void addFinderMethodForField(EclipseNode field) {
			FinderGenerator finderGenerator = new FinderGenerator(classNode, field);
			AbstractMethodDeclaration createdFinder = finderGenerator.createFinder();

			SetGeneratedByVisitor setGeneratedByVisitor = new SetGeneratedByVisitor(classNode.get());
			createdFinder.traverse(setGeneratedByVisitor, createdFinder.scope);
			
			if (!methodExists(createdFinder)) {
				injectMethod(classNode, createdFinder);
			}
		}
		
		// EclipseHandlerUtil.methodExists() does not check the argument types, so overloading
		// methods are not found.
		private boolean methodExists(AbstractMethodDeclaration createdFinder) {
			String finderName = String.valueOf(createdFinder.selector);
			for(EclipseNode node : classNode.down()) {
				if (node.get() instanceof MethodDeclaration) {
					MethodDeclaration methodDeclaration = (MethodDeclaration) node.get();

					String methodName = String.valueOf(methodDeclaration.selector);
					boolean namesEqual = finderName.equals(methodName);

					if (namesEqual) {
						boolean argumentTypesAreEqual = argumentTypesAreEqual(createdFinder.arguments, methodDeclaration.arguments);
						boolean found = namesEqual && argumentTypesAreEqual;
						if (found) {
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean argumentTypesAreEqual(Argument[] left, Argument[] right) {
			if (left.length == right.length) {
				for (int i = 0; i < right.length; i++) {
					// Compare String representation because TypeReference instances are not equals.
					String leftType = left[i].type.toString();
					String rightType = right[i].type.toString();
					boolean typeDifferenceFound = !leftType.equals(rightType);
					if (typeDifferenceFound) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		private void makeFieldFinal(EclipseNode field) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) field.get();
			fieldDeclaration.modifiers |= ClassFileConstants.AccFinal;
		}
		
		private void makeEnumPublic() {
			TypeDeclaration classDeclaration = (TypeDeclaration) classNode.get();
			classDeclaration.modifiers |= ClassFileConstants.AccPublic;
		}
	}
	
	
	
	private class FinderGenerator {
		private EclipseNode classNode;
		private EclipseNode fieldNode;
		private TypeDeclaration classDeclaration;
		private FieldDeclaration fieldDeclaration;
		private ASTNode source;
		private TypeReference classAsType;
		
		public FinderGenerator(EclipseNode classNode, EclipseNode fieldNode) {
			this.classNode = classNode;
			this.fieldNode = fieldNode;
			this.fieldDeclaration = (FieldDeclaration) fieldNode.get();
			this.classDeclaration = (TypeDeclaration) classNode.get();
			this.source = fieldNode.get();
			this.classAsType = cloneSelfType(fieldNode, classNode.get());
			
		}
		
		public MethodDeclaration createFinder() {
			MethodDeclaration method = new MethodDeclaration(classDeclaration.compilationResult);
			method.modifiers = modifiers();
			method.selector = methodName().toCharArray();
			method.returnType = classAsType;
			method.arguments = arguments();
			method.statements = body();
			return method;
		}
		
		private String methodName() {
			String name = fieldNode.getName();
			String finderName = "findBy" + firstCaseUpper(name);
			return finderName;
		}
		
		private String firstCaseUpper(String in) {
			if (in == null || in.length() < 1) {
				return in;
			} else {
				if (in.length() == 1) {
					return in.toUpperCase();
				} else {
					return in.substring(0, 1).toUpperCase() + in.substring(1);
				}
			}
		}
		
		private int modifiers() {
			return ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
		}
		
		private Argument[] arguments() {
			char[] argumentName = fieldNode.getName().toCharArray();
			long posNom = (long) source.sourceStart << 32 | source.sourceEnd;
			TypeReference argumentType = copyType(fieldDeclaration.type, source);
			int argumentModifier = Modifier.FINAL;
			Argument argument = new Argument(argumentName, posNom, argumentType, argumentModifier);
			return new Argument[] {argument};
		}
		
		// public static Colour findByName(String name) {
		//   for(Colour c : values()) {
		//     if (c.name.equals(name)) {  // 'c.name == name' for primitives
		//       return c;
		//     }
		//   }
		//   return null;
		// }
		private Statement[] body() {
			return new Statement[] {
				foreachLoop(), 
				finallyReturnNull()
			};
		}
		
		private Statement foreachLoop() {
			char[] nameOfIterationVariable = "value".toCharArray();

			// for( >>>Type value<<< : values() ) { ... }
			LocalDeclaration iterationVariable = new LocalDeclaration(nameOfIterationVariable, source.sourceStart, source.sourceEnd);;
			iterationVariable.type = classAsType;

			// for(Type value : >>>values()<<< ) { ... }
			MessageSend valuesMethod = new MessageSend();
			valuesMethod.selector = "values".toCharArray();
			valuesMethod.receiver = classAsQualifiedNameReference();
			
			// if (??condition??) return value;
			Expression condition = createFieldComparison(iterationVariable);
			Expression returnValue = new SingleNameReference(nameOfIterationVariable, source.sourceStart);
			Statement thenStatement = new ReturnStatement(returnValue, source.sourceStart, source.sourceEnd);;
			Statement comparisonStatement = new IfStatement(condition, asBlock(thenStatement), source.sourceStart, source.sourceEnd);
			
			// for( ... ) { ... }
			ForeachStatement foreachStatement = new ForeachStatement(iterationVariable, source.sourceStart);
			foreachStatement.collection = valuesMethod;
			foreachStatement.action = asBlock(comparisonStatement);
			
			return foreachStatement;
		}
		
		private QualifiedNameReference classAsQualifiedNameReference() {
			char[][] tokens = {classDeclaration.name};
			long[] pos = {0L};
			return new QualifiedNameReference(
				tokens, pos	, source.sourceStart, source.sourceEnd
			);
		}
		
		private Block asBlock(Statement... statements) {
			Block block = new Block(0);
			block.statements = statements;
			return block;
		}
		
		private Expression createFieldComparison(LocalDeclaration iterationVariable) {
			return fieldtypeIsPrimitive()
	             ? createPrimitiveComparison(iterationVariable)
	             : createObjectComparison(iterationVariable);
		}

		private boolean fieldtypeIsPrimitive() {
			return Eclipse.isPrimitive(fieldDeclaration.type);
		}

		// if( >>>value.$parameterName == $parameterName<<< ) ...
		private Expression createPrimitiveComparison(LocalDeclaration iterationVariable) {
			Expression left = HandleToString.generateQualifiedNameRef(source, "value".toCharArray(), fieldNode.getName().toCharArray());
			Expression right = new SingleNameReference(fieldNode.getName().toCharArray(), source.sourceStart);
			EqualExpression equalExpression = new EqualExpression(left, right, EqualExpression.EQUAL_EQUAL);
			return equalExpression;
		}
		
		// if( >>>value.$parameterName.equals($parameterName)<<< ) ...
		private Expression createObjectComparison(LocalDeclaration iterationVariable) {
			Expression foreachVariable = HandleToString.generateQualifiedNameRef(source, "value".toCharArray(), fieldNode.getName().toCharArray());
			Expression parameter = new SingleNameReference(fieldNode.getName().toCharArray(), source.sourceStart);
			
			MessageSend equals = new MessageSend();
			equals.receiver = foreachVariable;
			equals.selector = "equals".toCharArray();
			equals.arguments = new Expression[] {parameter};
			
			return equals;
		}
		
		// foreach(...) {...} >>>return null;<<<
		private Statement finallyReturnNull() {
			NullLiteral nullLiteral = new NullLiteral(source.sourceStart, source.sourceEnd);
			ReturnStatement returnStatement = new ReturnStatement(nullLiteral, source.sourceStart, source.sourceEnd);
			return returnStatement;
		}
	}

}
