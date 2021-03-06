package cz.ackee.wrapper;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import cz.ackee.wrapper.annotations.NoCompose;

/**
 * Method that will be wrapped with oauth handling
 */
public class WrapEnclosingMethod {
    public static final String TAG = WrapEnclosingMethod.class.getName();
    private Name methodName;
    private TypeMirror returnType;
    private boolean isPrivate;
    private List<? extends VariableElement> parameters;
    private boolean shouldWrap;
    private FoundType foundType = null;

    enum FoundType {
        OBSERVABLE, SINGLE, COMLETABLE
    }

    public WrapEnclosingMethod(ExecutableElement methodElement) {
        scanElement(methodElement);
    }

    public boolean isReachable() {
        return !isPrivate;
    }

    private void scanElement(ExecutableElement methodElement) {

        this.methodName = methodElement.getSimpleName();
        this.returnType = methodElement.getReturnType();


        this.isPrivate = methodElement.getModifiers().contains(Modifier.PRIVATE) ||
                methodElement.getModifiers().contains(Modifier.PROTECTED);
        this.parameters = methodElement.getParameters();
        this.shouldWrap = methodElement.getAnnotation(NoCompose.class) == null;
        if (returnType.getKind() == TypeKind.DECLARED) {
            DeclaredType type = (DeclaredType) returnType;
            TypeElement clz = (TypeElement) type.asElement();
            if (clz.getQualifiedName().toString().equals("io.reactivex.Observable")) {
                foundType = FoundType.OBSERVABLE;
            }
            if (clz.getQualifiedName().toString().equals("io.reactivex.Single")) {
                foundType = FoundType.SINGLE;
            }
            if (clz.getQualifiedName().toString().equals("io.reactivex.Completable")) {
                foundType = FoundType.COMLETABLE;
            }
        }
        this.shouldWrap = this.shouldWrap && foundType != null;
    }

    public MethodSpec generateCode() {

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName.toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(returnType));

        String observableType = null;
        if (shouldWrap && (foundType == FoundType.OBSERVABLE || foundType == FoundType.SINGLE)) {
            StringBuilder res = new StringBuilder();
            Util.typeToString(((DeclaredType) returnType).getTypeArguments().get(0), res, ',');
            observableType = res.toString();
        }

        String paramNames = "";
        for (VariableElement element : this.parameters) {
            if (paramNames.length() > 0) {
                paramNames += ", ";
            }
            builder.addParameter(TypeName.get(element.asType()), element.getSimpleName().toString());
            paramNames += element.getSimpleName().toString();
        }
        if (foundType != null && shouldWrap) {
            switch (foundType) {
                case OBSERVABLE:
                    builder.addStatement("return this.service.$L($L)$L", methodName.toString(), paramNames, ".<" + observableType + ">compose(this.rxWrapper.<" + observableType + ">wrapObservable())");
                    break;
                case SINGLE:
                    builder.addStatement("return this.service.$L($L)$L", methodName.toString(), paramNames, ".<" + observableType + ">compose(this.rxWrapper.<" + observableType + ">wrapSingle())");
                    break;
                case COMLETABLE:
                    builder.addStatement("return this.service.$L($L)$L", methodName.toString(), paramNames, ".compose(this.rxWrapper.wrapCompletable())");
                    break;
            }
        } else {
            builder.addStatement("return this.service.$L($L)", methodName.toString(), paramNames);
        }
        return builder.build();
    }

}
