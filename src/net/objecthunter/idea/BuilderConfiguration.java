package net.objecthunter.idea;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class BuilderConfiguration implements SearchableConfigurable {
    
    public static final String PLUGIN_ID = "net.objecthunter.idea.BuilderGenerator";
    public static final String PLUGIN_NAME = "Simple Builder Generator Plugin";
    
    private BuilderConfigurationUI configurationUI;
    
    private final BuilderPersistentState persistentState;
    
    public BuilderConfiguration(final Project project) {
        this.persistentState = BuilderPersistentState.getInstance(project);
    }
    
    @NotNull
    @Override
    public String getId() {
        return PLUGIN_ID;
    }
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return PLUGIN_NAME;
    }
    
    @Nullable
    @Override
    public JComponent createComponent() {
        this.configurationUI = new BuilderConfigurationUI(this.persistentState);
        return configurationUI.getRootPanel();
    }
    
    @Override
    public boolean isModified() {
        return this.configurationUI.isModified();
    }
    
    @Override
    public void reset() {
        this.configurationUI.reset();
    }
    
    @Override
    public void apply() throws ConfigurationException {
        this.persistentState.setMethodPrefix(this.configurationUI.getMethodPrefix());
    }
    
    @Override
    public void disposeUIResources() {
        this.configurationUI = null;
    }
}
