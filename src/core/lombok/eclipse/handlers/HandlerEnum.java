package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.Receiver;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.Enum;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleConstructor.SkipIfConstructorExists;

@ProviderFor(EclipseAnnotationHandler.class) public class HandlerEnum extends EclipseAnnotationHandler<lombok.Enum> {
	
	PrintWriter out = null;
	
	@Override public void handle(AnnotationValues<Enum> annotation, Annotation ast, EclipseNode annotationNode) {
		try {
			out = new PrintWriter(new FileWriter("C:\\projekte\\lombok\\lombok.log"));
			Generator generator = new Generator(annotationNode.up(), annotationNode);
			generator.generate();
		} catch (Exception e) {
			e.printStackTrace(out);
		} finally {
			out.close();
		}
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
			//createdFinder.traverse(setGeneratedByVisitor, createdFinder.scope);
			
			injectMethod(classNode, createdFinder);
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
			MethodDeclaration method = null;
			
			out.println("createFinder");
			out.printf("- name: %s%n", methodName());
			
			method = new MethodDeclaration(classDeclaration.compilationResult);
			method.modifiers = modifiers();
			method.selector = methodName().toCharArray();
			method.returnType = classAsType;
			method.arguments = arguments();
			method.statements = body();
			
			out.println(method);
			
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
			int pS = source.sourceStart, pE = source.sourceEnd;
			long p = (long) pS << 32 | pE;
			Argument argument = new Argument(fieldNode.getName().toCharArray(), p, copyType(fieldDeclaration.type, source), Modifier.FINAL);
			argument.sourceStart = pS;
			argument.sourceEnd = pE;
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
//				x(),
				foreachLoop(), 
				finallyReturnNull()
			};
		}
		
		private Statement x() {
			char[] nameOfIterationVariable = "value".toCharArray();
			LocalDeclaration iterationVariable = new LocalDeclaration(nameOfIterationVariable, source.sourceStart, source.sourceEnd);;
			iterationVariable.type = classAsType;
			iterationVariable.initialization = new NullLiteral(source.sourceStart, source.sourceEnd);
			fixASTNode(iterationVariable);
			return iterationVariable;
		}

		private Statement foreachLoop() {
			char[] nameOfIterationVariable = "value".toCharArray();

			// for( >>>Type value<<< : values() ) { ... }
			LocalDeclaration iterationVariable = new LocalDeclaration(nameOfIterationVariable, source.sourceStart, source.sourceEnd);;
			iterationVariable.type = classAsType;
			fixASTNode(iterationVariable);

			// if (??condition??) return value;
			Expression condition = createFieldComparison(iterationVariable);
			Expression returnValue = new SingleNameReference(nameOfIterationVariable, source.sourceStart);
			Statement thenStatement = new ReturnStatement(returnValue, source.sourceStart, source.sourceEnd);;
			Statement comparisonStatement = new IfStatement(condition, asBlock(thenStatement), source.sourceStart, source.sourceEnd);
			fixASTNode(condition);
			fixASTNode(returnValue);
			fixASTNode(thenStatement);
			fixASTNode(comparisonStatement);
			
			// for(Type value : >>>values()<<< ) { ... }
			MessageSend valuesMethod = new MessageSend();
			valuesMethod.selector = "values".toCharArray();
			valuesMethod.receiver = classAsQualifiedNameReference();
			fixASTNode(valuesMethod);
			
			// for( ... ) { ... }
			ForeachStatement foreachStatement = new ForeachStatement(iterationVariable, source.sourceStart);
			foreachStatement.collection = valuesMethod;
			foreachStatement.action = asBlock(comparisonStatement);
			fixASTNode(foreachStatement);
			
			return foreachStatement;
		}
		
		private QualifiedNameReference classAsQualifiedNameReference() {
			
			out.printf("classDecl.name='%s'%n", String.valueOf(classDeclaration.name));
			
			char[][] tokens = {"Farbe".toCharArray()};
			long[] pos = {0L};
			return new QualifiedNameReference(
				tokens, pos	, source.sourceStart, source.sourceEnd
			);
		}
		
		private void fixASTNode(ASTNode node) {
			node.sourceStart = source.sourceStart;
			node.sourceEnd = source.sourceEnd;
			setGeneratedBy(node, source);
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
			fixASTNode(equalExpression);
			return equalExpression;
		}
		
		// if( >>>value.$parameterName.equals($parameterName)<<< ) ...
		private Expression createObjectComparison(LocalDeclaration iterationVariable) {
			Expression foreachVariable = HandleToString.generateQualifiedNameRef(source, "value".toCharArray(), fieldNode.getName().toCharArray());
			Expression parameter = new SingleNameReference(fieldNode.getName().toCharArray(), source.sourceStart);
			
			MessageSend equals = new MessageSend();
			equals.sourceStart = source.sourceStart;
			equals.sourceEnd = source.sourceEnd;
			equals.receiver = foreachVariable;
			equals.selector = "equals".toCharArray();
			equals.arguments = new Expression[] {parameter};
			fixASTNode(equals);
			
			return equals;
		}
		
		// foreach(...) {...} >>>return null;<<<
		private Statement finallyReturnNull() {
			NullLiteral nullLiteral = new NullLiteral(source.sourceStart, source.sourceEnd);
			ReturnStatement returnStatement = new ReturnStatement(nullLiteral, source.sourceStart, source.sourceEnd);
			fixASTNode(returnStatement);
			return returnStatement;
		}
	}

}
