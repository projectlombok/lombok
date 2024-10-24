package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import lombok.ExtendsRecord;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

@Provides
public class HandleExtendsRecord extends JavacAnnotationHandler<ExtendsRecord> {

    @Override
    public void handle(AnnotationValues<ExtendsRecord> annotation, JCAnnotation ast, JavacNode annotationNode) {
        JavacNode typeNode = annotationNode.up();
        if (!(typeNode.get() instanceof JCClassDecl)) {
            annotationNode.addError("@ExtendsRecord can only be used on records.");
            return;
        }

        JCClassDecl classDecl = (JCClassDecl) typeNode.get();
        if ((classDecl.mods.flags & Flags.RECORD) == 0) {
            annotationNode.addError("@ExtendsRecord can only be used on records.");
            return;
        }

        Class<?> baseRecordClass = annotation.getInstance().value();
        String baseRecordClassName = baseRecordClass.getSimpleName();

        JavacTreeMaker maker = typeNode.getTreeMaker();
        List<JCVariableDecl> baseRecordFields = getBaseRecordFields(baseRecordClass, maker, annotationNode);

        List<JCVariableDecl> newFields = List.nil();
        for (JCVariableDecl field : baseRecordFields) {
            newFields = newFields.append(field);
        }
        for (JCTree member : classDecl.defs) {
            if (member instanceof JCVariableDecl) {
                newFields = newFields.append((JCVariableDecl) member);
            }
        }

        JCClassDecl newClassDecl = maker.ClassDef(
                classDecl.mods,
                classDecl.name,
                classDecl.typarams,
                classDecl.extending,
                classDecl.implementing,
                newFields
        );

        typeNode.replace(newClassDecl);
    }

    private List<JCVariableDecl> getBaseRecordFields(Class<?> baseRecordClass, JavacTreeMaker maker, JavacNode annotationNode) {
        List<JCVariableDecl> fields = List.nil();
        for (java.lang.reflect.RecordComponent component : baseRecordClass.getRecordComponents()) {
            JCVariableDecl field = maker.VarDef(
                    maker.Modifiers(Flags.PUBLIC | Flags.FINAL),
                    annotationNode.toName(component.getName()),
                    maker.Type(annotationNode.getTreeMaker().Ident(annotationNode.toName(component.getType().getSimpleName()))),
                    null
            );
            fields = fields.append(field);
        }
        return fields;
    }
}
