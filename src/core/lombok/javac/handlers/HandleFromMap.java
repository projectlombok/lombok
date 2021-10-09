package lombok.javac.handlers;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import lombok.FromMap;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.mangosdk.spi.ProviderFor;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.Javac.CTC_GREATER_OR_EQUAL;
import static lombok.javac.Javac.CTC_NOT;
import static lombok.javac.handlers.JavacHandlerUtil.*;

/**
 * Handles the {@code lombok.FromMap} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleFromMap extends JavacAnnotationHandler<FromMap> {

    public static final String RETURN_VAR_NAME = "rt";

    @Override public void handle(AnnotationValues<FromMap> annotation, JCAnnotation ast, JavacNode annotationNode) {
        handleFlagUsage(annotationNode, ConfigurationKeys.TO_STRING_FLAG_USAGE, "@FromMap");
        deleteAnnotationIfNeccessary(annotationNode, FromMap.class);
        generateFromMap(ast, annotationNode.up(), annotationNode);
    }

    public void generateFromMap(JCAnnotation ast, JavacNode typeNode, JavacNode source) {

        boolean notAClass = true;
        if (typeNode.get() instanceof JCClassDecl) {
            long flags = ((JCClassDecl) typeNode.get()).mods.flags;
            notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
        }

        if (notAClass) {
            source.addError("@FromMap is only supported on a class or enum.");
            return;
        }

        switch (methodExists("fromMap", typeNode, 0)) {
            case NOT_EXISTS:
                JCMethodDecl method = createFromMap(ast, typeNode, source);
                injectMethod(typeNode, method);
                break;
            case EXISTS_BY_LOMBOK:
                break;
            default:
            case EXISTS_BY_USER:
                source.addWarning("Not generating fromMap(): A method with that name already exists");
                break;
        }
    }

    static JCMethodDecl createFromMap(JCAnnotation ast, JavacNode typeNode, JavacNode source) {

        JavacTreeMaker maker = typeNode.getTreeMaker();

        // return type
        List<JCAnnotation> annsOnReturnType = List.nil();
        if (getCheckerFrameworkVersion(typeNode).generateUnique()) annsOnReturnType = List.of(maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__UNIQUE), List.<JCExpression>nil()));
        JCExpression returnType = namePlusTypeParamsToTypeReference(maker, typeNode, ((JCClassDecl) typeNode.get()).typarams, annsOnReturnType);

        // parameters
        JCExpression pType = chainDotsString(typeNode, "java.util.Map");
        ListBuffer<JCExpression> arguments = new ListBuffer<JCExpression>();
        arguments.append(genJavaLangTypeRef(typeNode, "String"));
        arguments.append(genJavaLangTypeRef(typeNode, "Object"));
        pType = maker.TypeApply(pType, arguments.toList());
        JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), typeNode.toName("map"), pType, null);
        List<JCVariableDecl> parameters = List.of(param);

        // build method body
        ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();

        // new Obj
        JCExpression newClass = maker.NewClass(null, List.<JCExpression>nil(), returnType, List.<JCExpression>nil(), null);
        statements.prepend(maker.VarDef(maker.Modifiers(Flags.FINAL), typeNode.toName(RETURN_VAR_NAME), returnType, newClass));

        // set
        for (JavacNode field : typeNode.down()) {

            if (field.getKind() != Kind.FIELD) continue;
            JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
            //Skip fields that start with $
            if (fieldDecl.name.toString().startsWith("$")) continue;
            //Skip static fields.
            if ((fieldDecl.mods.flags & Flags.STATIC) != 0) continue;
            //Skip final fields.
            if ((fieldDecl.mods.flags & Flags.FINAL) != 0) continue;


            String fieldTypeName = fieldDecl.getType().toString();
            String fieldName = fieldDecl.getName().toString();

            addPutStatements(typeNode, maker, statements, field, fieldTypeName, fieldName);
        }

        // return
        statements.append(maker.Return(maker.Ident(typeNode.toName(RETURN_VAR_NAME))));

        JCBlock methodBody = maker.Block(0, statements.toList());

        // method
        JCMethodDecl methodDef = maker.MethodDef(maker.Modifiers(Flags.PUBLIC | Flags.STATIC), typeNode.toName("fromMap"), returnType,
                List.<JCTypeParameter>nil(), parameters, List.<JCExpression>nil(), methodBody, null);
        return recursiveSetGeneratedBy(methodDef, typeNode.get(), source.getContext());
    }

    private static void addPutStatements(JavacNode typeNode,
                                         JavacTreeMaker maker,
                                         ListBuffer<JCStatement> statements,
                                         JavacNode field,
                                         String fieldTypeName,
                                         String fieldName) {
        JCTree.JCExpression getValueApply = maker.Apply(
                List.of(chainDots(typeNode, "java","lang","String")),
                maker.Select(
                        maker.Ident(typeNode.toName("map")),
                        typeNode.toName("get")
                ),
                List.of((JCExpression)maker.Literal(fieldName))
        );

        JCExpression getValueTypeCast = maker.TypeCast(((JCVariableDecl) field.get()).vartype, getValueApply);

        JCTree.JCExpression putFieldApply = maker.Apply(
                List.<JCExpression>of(chainDotsString(typeNode, fieldTypeName)),
                chainDotsString(typeNode, RETURN_VAR_NAME + "." + toSetterName(field)),
                List.of(getValueTypeCast)
        );

        JCExpression existKey = maker.Unary(CTC_NOT,
                maker.Apply(
                        List.of(chainDots(typeNode, "java","lang","String")),
                        maker.Select(
                                maker.Ident(typeNode.toName("map")),
                                typeNode.toName("containsKey")
                        ),
                        List.of((JCExpression)maker.Literal(fieldName))
                )
        );

        statements.append(maker.If(
                existKey,
                maker.Exec(putFieldApply),
                null
        ));

    }

    public static String initcap(String str) {
        if(str == null || "".equals(str)) {
            return str ;
        }
        if(str.length()== 1) {
            return str.toUpperCase() ;
        }
        return str.substring(0,1).toUpperCase() + str.substring(1) ;
    }

    public static String humpToLine(String str) {
        return str.replaceAll("[A-Z]", "_$0").toLowerCase();
    }
}

