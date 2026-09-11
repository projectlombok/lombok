package lombok.javac.handlers;

import static lombok.javac.Javac.CTC_BOT;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.LinkedList;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.Enum;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;

@ProviderFor(JavacAnnotationHandler.class)
public class HandlerEnum extends JavacAnnotationHandler<lombok.Enum> {

	@Override 
	public void handle(AnnotationValues<Enum> annotation, JCAnnotation ast, JavacNode annotationNode) {
		Generator generator = new Generator(annotationNode.up(), annotationNode);
		generator.generate();
	}



	private class Generator {
		private JavacNode classNode;
		private JavacNode annotationNode;
		
		public Generator(JavacNode classNode, JavacNode annotationNode) {
			this.classNode = classNode;
			this.annotationNode = annotationNode;
		}
		
		public void generate() {
			addPrivateAllArgsConstructor();
			for(JavacNode field : findAllFields()) {
				addFinderMethodForField(field);
				makeFieldFinal(field);
			}
			makeEnumPublic();
		}

		private void addPrivateAllArgsConstructor() {
			new HandleConstructor().generateAllArgsConstructor(
				classNode, 
				AccessLevel.PRIVATE, 
				null, 
				SkipIfConstructorExists.YES,
				annotationNode
			);
		}
		
		private java.util.List<JavacNode> findAllFields() {
			java.util.List<JavacNode> fields = new LinkedList<JavacNode>();
			for (JavacNode field : classNode.down()) {
				if (field.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
				//Skip fields that start with $
				if (fieldDecl.name.toString().startsWith("$")) continue;
				//Skip static fields.
				if ((fieldDecl.mods.flags & Flags.STATIC) != 0) continue;
				
				fields.add(field);
			}
			return fields;
		}
		
		private void addFinderMethodForField(JavacNode field) {
			FinderGenerator finderGenerator = new FinderGenerator(classNode, field);
			JCMethodDecl createdFinder = finderGenerator.createFinder();
			if (!methodExists(createdFinder)) {
				injectMethod(classNode, createdFinder);
			}
		}
		
		private boolean methodExists(JCMethodDecl createdFinder) {
			String finderName = createdFinder.getName().toString();
			for(JavacNode node : classNode.down()) {
				if (node.get() instanceof JCMethodDecl) {
					JCMethodDecl methodDeclaration = (JCMethodDecl) node.get();
					
					String methodName = methodDeclaration.getName().toString();
					boolean namesEqual = finderName.equals(methodName);

					if (namesEqual) {
						boolean argumentTypesAreEqual = argumentTypesAreEqual(createdFinder.getParameters(), methodDeclaration.getParameters());
						boolean found = namesEqual && argumentTypesAreEqual;
						if (found) {
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean argumentTypesAreEqual(List<JCVariableDecl> left, List<JCVariableDecl> right) {
			if (left.size() == right.size()) {
				for (int i = 0; i < right.size(); i++) {
					// Compare String representation because TypeReference instances are not equals.
					String leftType = left.get(i).toString();
					String rightType = right.get(i).toString();
					boolean typeDifferenceFound = !leftType.equals(rightType);
					if (typeDifferenceFound) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		private void makeFieldFinal(JavacNode field) {
			JCVariableDecl fieldDeclaration = (JCVariableDecl) field.get();
			fieldDeclaration.mods.flags |= Flags.FINAL;
		}

		private void makeEnumPublic() {
			JCClassDecl classDeclaration = (JCClassDecl)classNode.get();
			classDeclaration.mods.flags |= Flags.PUBLIC;
		}
	}
	
	
	
	private class FinderGenerator {
		private final List<JCExpression> JCE_BLANK = List.nil();

		private JavacNode classNode;
		private JavacNode field;
		private JCVariableDecl fieldDeclaration;
		private JavacTreeMaker make;

		public FinderGenerator(JavacNode classNode, JavacNode field) {
			this.classNode = classNode;
			this.field = field;
			this.fieldDeclaration = (JCVariableDecl) field.get();
			this.make = field.getTreeMaker();
		}
		
		public JCMethodDecl createFinder() {
			List<JCTypeParameter> methodGenericParams = List.nil();
			List<JCExpression> throwsClauses = List.nil();
			JCExpression annotationMethodDefaultValue = null;
			JCMethodDecl method = recursiveSetGeneratedBy(
				make.MethodDef(
					modifiers(), 
					methodName(), returnType(), methodGenericParams, 
					parameters(), throwsClauses, 
					methodBody(), annotationMethodDefaultValue
				), 
				classNode.get(), 
				field.getContext()
			);
			return method;
		}

		private JCModifiers modifiers() {
			return make.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL);
		}

		private Name methodName() {
			String name = field.getName();
			String finderName = "findBy" + firstCaseUpper(name);
			Name methodName = field.toName(finderName);
			return methodName;
		}
		
		private String firstCaseUpper(String in) {
			if (in == null || in.length() < 1) {
				return in;
			} else {
				if (in.length() == 1) {
					return in.toUpperCase();
				} else {
					return in.substring(0, 1).toUpperCase()
						 + in.substring(1);
				}
			}
		}
		
		private JCExpression returnType() {
			JCClassDecl classDeclaration = (JCClassDecl)classNode.get();
			JCExpression returnType = make.Ident(classDeclaration.name);
			return returnType;
		}

		private List<JCVariableDecl> parameters() {
			JCVariableDecl parameterDeclaration = make.VarDef(
				make.Modifiers(Flags.PARAMETER),
				fieldDeclaration.name,
				fieldDeclaration.vartype,
				null
			);
			List<JCVariableDecl> parameters = List.of(parameterDeclaration);
			return parameters;
		}

		/**
		 * Creates the method Body of the finder
		 * <pre>
		 * for (MyEnum value : values()) {
		 *     if (value.field == field) {
		 *         return value;
		 *     }
		 * }
		 * return null;
		 * </pre>
		 * For object types the comparison is done via equals() method:
		 * <pre>
		 * ...
		 *     if (value.field.equals(field)) {
		 * ...
		 * </pre>
		 * @return
		 */
		private JCBlock methodBody() {
			JCVariableDecl var = make.VarDef(
				make.Modifiers(0), 
				getName("value"),
				returnType(), 
				null
			);

			JCExpression expr = make.Apply(
				JCE_BLANK,
				make.Select(
					make.Ident(getName(classNode.getName())),
					getName("values")
				),
				JCE_BLANK
			);
			
			JCExpression cond = createFieldComparison();
			JCStatement thenpart = asBlock(
				make.Return(make.Ident(getName("value")))
			);
			JCStatement body = asBlock(
				make.If(cond, thenpart, null)
			);
			JCEnhancedForLoop foreachLoop = make.ForeachLoop(var, expr, body);
			
			JCLiteral nullLiteral = make.Literal(CTC_BOT, null);
			JCReturn finalReturnStatement = make.Return(nullLiteral);
			
			JCBlock methodBody = asBlock(
				foreachLoop,
				finalReturnStatement
			);
			return methodBody;
		}

		private JCExpression createFieldComparison() {
			return Javac.isPrimitive(fieldDeclaration.vartype)
                 ? createPrimitiveComparison()
                 : createObjectComparison();
		}
		
		private JCExpression createPrimitiveComparison() {
			// this.field == field
			return make.Binary(
				Javac.CTC_EQUAL,
				chainDots(classNode, "value", field.getName()),
				make.Ident(getName(field.getName()))
			);
		}
		
		private JCExpression createObjectComparison() {
			// value.field.equals(field)
			return make.Apply(
				JCE_BLANK,
				make.Select(
					chainDots(classNode, "value", field.getName()),
					getName("equals")
				),
				asList(make.Ident(getName(field.getName())))
			);
		}

		private Name getName(String name) {
			return classNode.toName(name);
		}
		
		private JCBlock asBlock(JCStatement... statements) {
			List<JCStatement> statementList = List.from(statements);
			return make.Block(Flags.BLOCK, statementList);
		}
		
		private List<JCExpression> asList(JCExpression... expr) {
			return List.from(expr);
		}
	}
	
}
