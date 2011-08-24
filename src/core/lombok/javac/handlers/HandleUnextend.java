/*
 * Copyright © 2010-2011 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.chainDots;
import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import lombok.Unextend;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.FindTypeVarScanner;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.JavacResolution.TypeNotConvertibleException;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.Unextend;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleUnextend extends JavacAnnotationHandler<Unextend> {

    @Override
    public boolean isResolutionBased() {
        return true;
    }
    private static final List<String> METHODS_IN_OBJECT = Collections.unmodifiableList(Arrays.asList(
            "hashCode()",
            "canEqual(java.lang.Object)", //Not in j.l.Object, but it goes with hashCode and equals so if we ignore those two, we should ignore this one.
            "equals(java.lang.Object)",
            "wait()",
            "wait(long)",
            "wait(long, int)",
            "notify()",
            "notifyAll()",
            "toString()",
            "getClass()",
            "clone()",
            "finalize()"));

    @Override
    public void handle(AnnotationValues<Unextend> annotation, JCAnnotation ast, JavacNode annotationNode) {
        System.err.println("Annotation: " + annotation);
        deleteAnnotationIfNeccessary(annotationNode, Unextend.class);
        if (annotationNode.up().getKind() != Kind.TYPE) {
            return;
        }

        List<Object> unextendTypes = annotation.getActualExpressions("types");
        List<Object> excludeTypes = annotation.getActualExpressions("excludes");
        JavacResolution reso = new JavacResolution(annotationNode.getContext());
        List<Type> toUnextend = new ArrayList<Type>();
        List<Type> toExclude = new ArrayList<Type>();

        if (unextendTypes.isEmpty()) {
            Type type = ((JCVariableDecl) annotationNode.up().get()).type;
            if (type == null) {
                reso.resolveClassMember(annotationNode.up());
            }
            //TODO I'm fairly sure the above line (and that entire method) does effectively bupkis!
            type = ((JCVariableDecl) annotationNode.up().get()).type;
            if (type != null) {
                toUnextend.add(type);
            }
        } else {
            for (Object dt : unextendTypes) {
                System.err.println("dt = " + dt);
                if (dt instanceof JCFieldAccess && ((JCFieldAccess) dt).name.toString().equals("class")) {
                    System.err.println("dt looks like a class = " + dt);
                    Type type = ((JCFieldAccess) dt).selected.type;
                    if (type == null) {
                        reso.resolveClassMember(annotationNode);
                    }
                    type = ((JCFieldAccess) dt).selected.type;
                    if (type != null) {
                        toUnextend.add(type);
                    }
                }
            }
        }

        for (Object et : excludeTypes) {
            if (et instanceof JCFieldAccess && ((JCFieldAccess) et).name.toString().equals("class")) {
                Type type = ((JCFieldAccess) et).selected.type;
                if (type == null) {
                    reso.resolveClassMember(annotationNode);
                }
                type = ((JCFieldAccess) et).selected.type;
                if (type != null) {
                    toExclude.add(type);
                }
            }
        }

        List<MethodSig> signaturesToUnextend = new ArrayList<MethodSig>();
        List<MethodSig> signaturesToExclude = new ArrayList<MethodSig>();
        Set<String> banList = new HashSet<String>();
        banList.addAll(METHODS_IN_OBJECT);

        //To exclude all methods in the class itself, try this:
        for (Symbol member : ((JCClassDecl) annotationNode.up().get()).sym.getEnclosedElements()) {
            if (member instanceof MethodSymbol) {
                MethodSymbol method = (MethodSymbol) member;
                banList.add(printSig((ExecutableType) method.asType(), method.name, annotationNode.getTypesUtil()));
            }
        }

        for (Type tt : toExclude) {
            if (tt instanceof ClassType) {
                ClassType ct = (ClassType) tt;
                TypeSymbol tsym = ct.asElement();
                if (tsym == null) {
                    return;
                }
                for (Symbol member : tsym.getEnclosedElements()) {
                    if (member instanceof MethodSymbol) {
                        MethodSymbol method = (MethodSymbol) member;
                        banList.add(printSig((ExecutableType) method.asType(), method.name, annotationNode.getTypesUtil()));
                    }
                }
            } else {
                annotationNode.addError("@Delegate can only use concrete class types, not wildcards, arrays, type variables, or primitives.");
                return;
            }
        }
        for (MethodSig sig : signaturesToExclude) {
            banList.add(printSig(sig.type, sig.name, annotationNode.getTypesUtil()));
        }
        System.err.println("banlist: " + banList);
        for (Type t : toUnextend) {
            if (t instanceof ClassType) {
                ClassType ct = (ClassType) t;
                addMethodBindings(signaturesToUnextend, ct, annotationNode, banList);
            } else {
                annotationNode.addError("@Unextend can only use concrete class types, not wildcards, arrays, type variables, or primitives.");
                return;
            }
        }

        Name unextendFieldName = annotationNode.toName(annotationNode.up().getName());

        for (MethodSig sig : signaturesToUnextend) {
            generateAndAdd(sig, annotationNode, unextendFieldName);
        }
    }

    private void generateAndAdd(MethodSig sig, JavacNode annotation, Name unextendFieldName) {
        List<JCMethodDecl> toAdd = new ArrayList<JCMethodDecl>();
        try {
            toAdd.add(createUnextendMethod(sig, annotation, unextendFieldName));
        } catch (TypeNotConvertibleException e) {
            annotation.addError("Can't create unextend method for " + sig.name + ": " + e.getMessage());
            return;
        } catch (CantMakeUnextends e) {
            annotation.addError("There's a conflict in the names of type parameters. Fix it by renaming the following type parameters of your class: " + e.conflicted);
            return;
        }

        for (JCMethodDecl method : toAdd) {
            JavacHandlerUtil.injectMethod(annotation.up(), method);
        }
    }

    private static class CantMakeUnextends extends Exception {

        Set<String> conflicted;
    }

    /**
     * There's a rare but problematic case if a unextend method has its own type variables, and the unextendd type does too, and the method uses both.
     * If for example the unextendd type has {@code <E>}, and the method has {@code <T>}, but in our class we have a {@code <T>} at the class level, then we have two different
     * type variables both named {@code T}. We detect this situation and error out asking the programmer to rename their type variable.
     * 
     * @throws CantMakeUnextends If there's a conflict. Conflict list is in ex.conflicted.
     */
    private void checkConflictOfTypeVarNames(MethodSig sig, JavacNode annotation) throws CantMakeUnextends {
        // As first step, we check if there's a conflict between the unextend method's type vars and our own class.

        if (sig.elem.getTypeParameters().isEmpty()) {
            return;
        }
        Set<String> usedInOurType = new HashSet<String>();

        JavacNode enclosingType = annotation;
        while (enclosingType != null) {
            if (enclosingType.getKind() == Kind.TYPE) {
                List<JCTypeParameter> typarams = ((JCClassDecl) enclosingType.get()).typarams;
                if (typarams != null) {
                    for (JCTypeParameter param : typarams) {
                        if (param.name != null) {
                            usedInOurType.add(param.name.toString());
                        }
                    }
                }
            }
            enclosingType = enclosingType.up();
        }

        Set<String> usedInMethodSig = new HashSet<String>();
        for (TypeParameterElement param : sig.elem.getTypeParameters()) {
            usedInMethodSig.add(param.getSimpleName().toString());
        }

        usedInMethodSig.retainAll(usedInOurType);
        if (usedInMethodSig.isEmpty()) {
            return;
        }

        // We might be delegating a List<T>, and we are making method <T> toArray(). A conflict is possible.
        // But only if the toArray method also uses type vars from its class, otherwise we're only shadowing,
        // which is okay as we'll add a @SuppressWarnings.
        FindTypeVarScanner scanner = new FindTypeVarScanner();
        sig.elem.asType().accept(scanner, null);
        Set<String> names = new HashSet<String>(scanner.getTypeVariables());
        names.removeAll(usedInMethodSig);
        if (!names.isEmpty()) {
            // We have a confirmed conflict. We could dig deeper as this may still be a false alarm, but its already an exceedingly rare case.
            CantMakeUnextends cmd = new CantMakeUnextends();
            cmd.conflicted = usedInMethodSig;
            throw cmd;
        }
    }

    private JCMethodDecl createUnextendMethod(MethodSig sig, JavacNode annotation, Name unextendFieldName) throws TypeNotConvertibleException, CantMakeUnextends {
        /* public <T, U, ...> ReturnType methodName(ParamType1 name1, ParamType2 name2, ...) throws T1, T2, ... {
         *      (return) unextend.<T, U>methodName(name1, name2);
         *  }
         */

        checkConflictOfTypeVarNames(sig, annotation);

        TreeMaker maker = annotation.getTreeMaker();

        com.sun.tools.javac.util.List<JCAnnotation> annotations;
        if (sig.isDeprecated) {
            annotations = com.sun.tools.javac.util.List.of(maker.Annotation(
                    JavacHandlerUtil.chainDots(maker, annotation, "java", "lang", "Deprecated"),
                    com.sun.tools.javac.util.List.<JCExpression>nil()));
        } else {
            annotations = com.sun.tools.javac.util.List.nil();
        }

        JCModifiers mods = maker.Modifiers(Flags.PUBLIC, annotations);
        JCExpression returnType = JavacResolution.typeToJCTree((Type) sig.type.getReturnType(), maker, annotation.getAst(), true);
        System.err.println("return type: " + returnType + " :: " + sig);
        boolean useReturn = sig.type.getReturnType().getKind() != TypeKind.VOID;
        ListBuffer<JCVariableDecl> params = sig.type.getParameterTypes().isEmpty() ? null : new ListBuffer<JCVariableDecl>();
        ListBuffer<JCExpression> args = sig.type.getParameterTypes().isEmpty() ? null : new ListBuffer<JCExpression>();
        ListBuffer<JCExpression> thrown = sig.type.getThrownTypes().isEmpty() ? null : new ListBuffer<JCExpression>();
        ListBuffer<JCTypeParameter> typeParams = sig.type.getTypeVariables().isEmpty() ? null : new ListBuffer<JCTypeParameter>();
        ListBuffer<JCExpression> typeArgs = sig.type.getTypeVariables().isEmpty() ? null : new ListBuffer<JCExpression>();
        Types types = Types.instance(annotation.getContext());

        if (typeParams != null) {
            System.err.println("type params: ");
            for (JCTypeParameter pppp : typeParams.elems) {
                System.err.println("    :" + pppp.getName());
            }
        }
        if (typeArgs != null) {
            System.err.println("type args: " + (typeParams == null ? null : typeArgs.elems));
        }
        for (TypeMirror param : sig.type.getTypeVariables()) {
            Name name = ((TypeVar) param).tsym.name;
            typeParams.append(maker.TypeParameter(name, maker.Types(types.getBounds((TypeVar) param))));
            typeArgs.append(maker.Ident(name));
        }

        for (TypeMirror ex : sig.type.getThrownTypes()) {
            thrown.append(JavacResolution.typeToJCTree((Type) ex, maker, annotation.getAst(), true));
        }

        int idx = 0;
        for (TypeMirror param : sig.type.getParameterTypes()) {
            JCModifiers paramMods = maker.Modifiers(Flags.FINAL);
            String[] paramNames = sig.getParameterNames();
            Name name = annotation.toName(paramNames[idx++]);
            params.append(maker.VarDef(paramMods, name, JavacResolution.typeToJCTree((Type) param, maker, annotation.getAst(), true), null));
            args.append(maker.Ident(name));
        }


        /*		JCExpression unextendFieldRef = maker.Select(maker.Ident(annotation.toName("this")), unextendFieldName);
        
        JCExpression unextendCall = maker.Apply(toList(typeArgs), maker.Select(unextendFieldRef, sig.name), toList(args));
        JCStatement body = useReturn ? maker.Return(unextendCall) : maker.Exec(unextendCall);
        JCBlock bodyBlock = maker.Block(0, com.sun.tools.javac.util.List.of(body));
         */

        /*
         *             JCExpression lombokLombokSneakyThrowNameRef = chainDots(maker, annotation, new String[]{"java", "lang", "UnsupportedOperationException"});
        JCBlock bodyBlock = maker.Block(0,
        com.sun.tools.javac.util.List.<JCStatement>of(
        maker.Throw(
        maker.Create(maker.Ident(annotation.toName("$ex")).asElement(),
        com.sun.tools.javac.util.List.<JCExpression>of(maker.Literal("Unimplemented"))))));
        
         */
//                JCExpression lombokLombokSneakyThrowNameRef = chainDots(maker, annotation, new String[] {"java", "lang", "UnsupportedOperationException"});
//		JCBlock bodyBlock = maker.Block(0, com.sun.tools.javac.util.List.<JCStatement>of(maker.Throw(maker.Apply(
//				com.sun.tools.javac.util.List.<JCExpression>nil(), lombokLombokSneakyThrowNameRef,
//				com.sun.tools.javac.util.List.<JCExpression>of(maker.Literal("Unimplemented"))))));
        JCExpression javaLangStringRef = chainDots(maker, annotation, new String[]{"java", "lang", "String"});
        JCExpression javaLangUOERef = chainDots(maker, annotation, new String[]{"java", "lang", "UnsupportedOperationException"});
        JCBlock bodyBlock = maker.Block(0, com.sun.tools.javac.util.List.<JCStatement>of(
                maker.Throw(
                maker.NewClass(null,
                null,//com.sun.tools.javac.util.List.<JCExpression>of(javaLangStringRef),
                javaLangUOERef,
                com.sun.tools.javac.util.List.<JCExpression>of(maker.Literal("Unimplemented")),
                null))));

        JCMethodDecl methodDef = maker.MethodDef(mods,
                sig.name,
                returnType,
                toList(typeParams),
                toList(params),
                toList(thrown),
                bodyBlock,
                null);
        return Javac.recursiveSetGeneratedBy(methodDef, annotation.get());
    }

    private static <T> com.sun.tools.javac.util.List<T> toList(ListBuffer<T> collection) {
        return collection == null ? com.sun.tools.javac.util.List.<T>nil() : collection.toList();
    }

    private void addMethodBindings(List<MethodSig> signatures, ClassType ct, JavacNode node, Set<String> banList) {
        TypeSymbol tsym = ct.asElement();
        if (tsym == null) {
            return;
        }
        for (Symbol member : tsym.getEnclosedElements()) {
            System.err.println("member: " + member);
            if (member.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (member.isStatic()) {
                continue;
            }
            if (member.isConstructor()) {
                continue;
            }
            ExecutableElement exElem = (ExecutableElement) member;
            System.err.println("Modifiers: " + exElem.getModifiers());
            if (!exElem.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            if (exElem.getModifiers().contains(Modifier.FINAL)) {
                System.err.println("element is final: " + exElem);
                continue;
            }
            ExecutableType methodType = (ExecutableType) node.getTypesUtil().asMemberOf(ct, member);
            String sig = printSig(methodType, member.name, node.getTypesUtil());
            if (!banList.add(sig)) {
                continue; //If add returns false, it was already in there
            }
            boolean isDeprecated = exElem.getAnnotation(Deprecated.class) != null;
            signatures.add(new MethodSig(member.name, methodType, isDeprecated, exElem));
        }

        if (ct.supertype_field instanceof ClassType) {
            System.err.println("case 1");
            addMethodBindings(signatures, (ClassType) ct.supertype_field, node, banList);
        }
        if (ct.interfaces_field != null) {
            System.err.println("case 2");
            for (Type iface : ct.interfaces_field) {
                if (iface instanceof ClassType) {
                    addMethodBindings(signatures, (ClassType) iface, node, banList);
                }
            }
        }
    }

    private static class MethodSig {

        final Name name;
        final ExecutableType type;
        final boolean isDeprecated;
        final ExecutableElement elem;

        MethodSig(Name name, ExecutableType type, boolean isDeprecated, ExecutableElement elem) {
            this.name = name;
            this.type = type;
            this.isDeprecated = isDeprecated;
            this.elem = elem;
        }

        String[] getParameterNames() {
            List<? extends VariableElement> paramList = elem.getParameters();
            String[] paramNames = new String[paramList.size()];
            for (int i = 0; i < paramNames.length; i++) {
                paramNames[i] = paramList.get(i).getSimpleName().toString();
            }
            return paramNames;
        }

        @Override
        public String toString() {
            return (isDeprecated ? "@Deprecated " : "") + name + " " + type;
        }
    }

    private static String printSig(ExecutableType method, Name name, JavacTypes types) {
        StringBuilder sb = new StringBuilder();
        sb.append(name.toString()).append("(");
        boolean first = true;
        for (TypeMirror param : method.getParameterTypes()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(typeBindingToSignature(param, types));
        }
        return sb.append(")").toString();
    }

    private static String typeBindingToSignature(TypeMirror binding, JavacTypes types) {
        binding = types.erasure(binding);
        return binding.toString();
    }
}
