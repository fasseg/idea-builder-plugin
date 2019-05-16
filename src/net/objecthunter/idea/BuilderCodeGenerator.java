package net.objecthunter.idea;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BuilderCodeGenerator {

    /* Holds the class that the builder will be added to */
    private final PsiClass clazz;

    /* The IntelliJ Idea ElementFactory for creating new classes and such */
    private final PsiElementFactory elementFactory;

    /* Holds the fields for which builder methods will be generated */
    private final List<PsiField> fields;

    /* Holds the name for the static inner builder class */
    private static final String builderClassName = "Builder";

    private static final String builderParamName = builderClassName.toLowerCase();
    
    /* The builder configuration is stored in the persistent state instance */
    private final BuilderPersistentState persistentState;
    
    public BuilderCodeGenerator(final PsiClass clazz, final BuilderPersistentState persistentState) {
        this.clazz = clazz;
        this.elementFactory = JavaPsiFacade.getElementFactory(clazz.getProject());
        this.persistentState = persistentState;
        this.fields = getFieldsFromClass(clazz, this.persistentState.isConsiderFinalFieldsOnly());
    }

    private static List<PsiField> getFieldsFromClass(final PsiClass clazz, final boolean finalOnly) {
        final List<PsiField> fields = new ArrayList<>();
        for (final PsiField field : clazz.getAllFields()) {
            if (field.getModifierList().hasExplicitModifier(PsiModifier.FINAL)) {
                fields.add(field);
            } else if (!finalOnly) {
                fields.add(field);
            }
        }
        return fields;
    }

    public void removeBuilder() {
        for (final PsiClass innerClass : clazz.getAllInnerClasses()) {
            if (innerClass.getName().equals(builderClassName)) {
                /* delete existing constructors for this builder */
                this.removeConstructor();
                /* delete the builder */
                innerClass.delete();
                return;
            }
        }
    }

    private void removeConstructor() {
        for (final PsiMethod constructor : this.clazz.getConstructors()) {
            if (isConstructorForCurrentBuilder(constructor)) {
                constructor.delete();
            }
        }
    }

    private boolean isConstructorForCurrentBuilder(final PsiMethod constructor) {
        if (constructor.getParameterList().getParameters().length == 1) {
            final PsiParameter param = constructor.getParameterList().getParameters()[0];
            if (param.getType().getPresentableText().equals(builderClassName) && param.getName().equals(builderParamName)) {
                return true;
            }
        }
        return false;
    }

    public void generateBuilder() {
        /* remove any existing builder class before generating the new code */
        this.removeBuilder();

        /* create a new builder class that will be added to the parent class */
        final PsiClass builderClass = createBuilderClass();

        /* add Javadoc to the builder class */
        builderClass.addBefore(createBuilderJavaDoc(), builderClass.getFirstChild());

        /* Add a constructor to the target class */
        this.clazz.add(createConstructor(builderClass));
        
        /* Create a static method returning the builder in the static class */
        if (this.persistentState.isCreateStaticBuilderMethod()) {
            this.clazz.add(this.createStaticBuilderMethod());
        }

        /* add Javadoc to the builder methods */
        for (final PsiMethod method : builderClass.getMethods()) {
            method.addBefore(createMethodJavaDoc(method), method.getFirstChild());
        }

        this.clazz.add(builderClass);
    }
    
    private PsiElement createStaticBuilderMethod() {
        final PsiMethod staticBuilderMethod = elementFactory.createMethod("builder", elementFactory.createType(elementFactory.createClass(this.builderClassName)));
        staticBuilderMethod.getModifierList().setModifierProperty(PsiModifier.PUBLIC, true);
        staticBuilderMethod.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        staticBuilderMethod.getBody().add(elementFactory.createStatementFromText("return new Builder();", null));
        return staticBuilderMethod;
    }
    
    private PsiMethod createConstructor(final PsiClass builderClass) {
        final PsiMethod constructor = elementFactory.createConstructor();
        constructor.getParameterList().add(elementFactory.createParameter(builderParamName, elementFactory.createType(builderClass)));
        constructor.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);
        for (final PsiField field : fields) {
            constructor.getBody().add(elementFactory.createStatementFromText("this." + field.getName() + "=builder." + field.getName() + ";", null));
        }
        constructor.addBefore(createConstructorJavaDoc(), constructor.getFirstChild());
        return constructor;
    }

    private PsiClass createBuilderClass() {
        final PsiClass builderClass = elementFactory.createClass(this.builderClassName);
        builderClass.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
        builderClass.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        builderClass.getModifierList().setModifierProperty(PsiModifier.PUBLIC, true);

        for (final PsiField field : createBuilderFields()) {
            builderClass.add(field);
        }

        for (final PsiMethod method : createBuilderMethods(elementFactory.createType(builderClass))) {
            builderClass.add(method);
        }

        return builderClass;
    }

    private PsiDocComment createBuilderJavaDoc() {
        final StringBuilder comment = new StringBuilder();
        comment.append("/**\n")
                .append(" * Builder for instances of type {@link ")
                .append(this.clazz.getQualifiedName())
                .append("}")
                .append("\n */\n");
        return elementFactory.createDocCommentFromText(comment.toString());
    }

    private PsiDocComment createConstructorJavaDoc() {
        final StringBuilder comment = new StringBuilder();
        comment.append("/**\n")
                .append(" * Constructor for instances of type {@link ")
                .append(this.clazz.getQualifiedName())
                .append("} using the Builder implementation")
                .append("\n */\n");
        return elementFactory.createDocCommentFromText(comment.toString());
    }

    private PsiDocComment createMethodJavaDoc(final PsiMethod method) {
        final StringBuilder comment = new StringBuilder();
        if (method.getName().equals("build")) {
            comment.append("/**\n")
                    .append(" * Create a new instance of type {@link ")
                    .append(this.clazz.getQualifiedName())
                    .append("}\n*/");
        }else {
            comment.append("/**\n")
                    .append(" * Set the value of the field ")
                    .append(this.inferFieldName(method.getName()))
                    .append(" of the target instance of type {@link ")
                    .append(this.clazz.getQualifiedName())
                    .append("}\n */\n");
        }
        return elementFactory.createDocCommentFromText(comment.toString());

    }
    
    private String inferFieldName(final String methodName) {
        if (StringUtils.isEmpty(this.persistentState.getMethodPrefix())) {
            return methodName;
        }
        String inferredName = methodName.substring(this.persistentState.getMethodPrefix().length());
        inferredName = inferredName.substring(0,1).toLowerCase() + (inferredName.length() > 1 ? inferredName.substring(1) : "");
        return inferredName;
    }
    
    private List<PsiField> createBuilderFields() {
        final List<PsiField> builderFields = new ArrayList<>();
        for (final PsiField field : fields) {
            final PsiField builderField = elementFactory.createField(field.getName(), field.getType());
            builderField.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);
            builderFields.add(builderField);
        }
        return builderFields;
    }

    private List<PsiMethod> createBuilderMethods(final PsiType builderType) {
        final List<PsiMethod> methods = new ArrayList<>();
        for (final PsiField field : fields) {
            final PsiMethod method = elementFactory.createMethod(this.createMethodName(field.getName()), builderType);
            final PsiParameter param = elementFactory.createParameter(field.getName(), field.getType());
            param.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
            method.getParameterList().add(param);
            method.getBody().add(elementFactory.createStatementFromText("this." + field.getName() + "=" + field.getName() + ";", method));
            method.getBody().add(elementFactory.createStatementFromText("return this;", method));
            methods.add(method);
        }

        final PsiMethod buildMethod = elementFactory.createMethod("build", elementFactory.createType(clazz));
        buildMethod.getBody().add(elementFactory.createStatementFromText("return new " + clazz.getName() + "(this);", buildMethod));
        methods.add(buildMethod);
        return methods;
    }
    
    private String createMethodName(final String name) {
        final String prefix = this.persistentState.getMethodPrefix();
        if (StringUtils.isEmpty(prefix)) {
            return name;
        } else {
            return prefix + name.substring(0,1).toUpperCase() + (name.length() > 1 ? name.substring(1) : "");
        }
    }
    
}