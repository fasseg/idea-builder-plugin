package net.objecthunter.idea;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "BuilderPersistentState", storages = {@Storage(BuilderConfiguration.PLUGIN_ID + ".xml")})
public class BuilderPersistentState implements PersistentStateComponent<BuilderPersistentState> {

    private String methodPrefix;
    
    private boolean considerFinalFieldsOnly;
    
    private boolean createStaticBuilderMethod;
    
    @Nullable
    @Override
    public BuilderPersistentState getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull final BuilderPersistentState builderPersistentState) {
        XmlSerializerUtil.copyBean(builderPersistentState, this);
    }
    
    public static BuilderPersistentState getInstance(final Project project) {
        return ServiceManager.getService(project, BuilderPersistentState.class);
    }
    
    public String getMethodPrefix() {
        if (this.methodPrefix == null) {
            return "";
        }
        return this.methodPrefix;
    }
    
    public void setMethodPrefix(final String methodPrefix) {
        this.methodPrefix = methodPrefix;
    }
    
    public boolean isConsiderFinalFieldsOnly() {
        return considerFinalFieldsOnly;
    }
    
    public void setConsiderFinalFieldsOnly(final boolean considerFinalFieldsOnly) {
        this.considerFinalFieldsOnly = considerFinalFieldsOnly;
    }
    
    public boolean isCreateStaticBuilderMethod() {
        return createStaticBuilderMethod;
    }
    
    public void setCreateStaticBuilderMethod(final boolean createStaticBuilderMethod) {
        this.createStaticBuilderMethod = createStaticBuilderMethod;
    }
}
