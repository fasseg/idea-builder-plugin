package net.objecthunter.idea;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;

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

    public BuilderCodeGenerator(final PsiClass clazz) {
        this.clazz = clazz;
        this.elementFactory = JavaPsiFacade.getElementFactory(clazz.getProject());
        this.fields = getFinalFieldsFromClass(clazz);
    }

    private static List<PsiField> getFinalFieldsFromClass(PsiClass clazz) {
        final List<PsiField> fields = new ArrayList<>();
        for (final PsiField field : clazz.getAllFields()) {
            if (field.getModifierList().hasExplicitModifier(PsiModifier.FINAL)) {
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

        /* add Javadoc to the buidler class */
        builderClass.addBefore(createBuilderJavaDoc(), builderClass.getFirstChild());

        final PsiMethod constructor = createConstructor(builderClass);
        this.clazz.add(constructor);

        /* add Javadoc to the builder methods */
        for (final PsiMethod method : builderClass.getMethods()) {
            method.addBefore(createMethodJavaDoc(method), method.getFirstChild());
        }

        this.clazz.add(builderClass);
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
                    .append(method.getName())
                    .append(" of the target instance of type {@link ")
                    .append(this.clazz.getQualifiedName())
                    .append("}\n */\n");
        }
        return elementFactory.createDocCommentFromText(comment.toString());

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
            final PsiMethod method = elementFactory.createMethod(field.getName(), builderType);
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

}