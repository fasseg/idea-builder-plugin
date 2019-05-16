package net.objecthunter.idea;

import javax.swing.*;

public class BuilderConfigurationUI {
    
    private JTextField fieldPrefix;
    
    private JPanel rootPanel;
    
    private JCheckBox checkBoxFinalFieldsOnly;
    
    private JCheckBox checkBoxCreateStaticBuilderMethod;
    
    private BuilderPersistentState persistentState;
    
    public BuilderConfigurationUI(final BuilderPersistentState persistentState) {
        this.persistentState = persistentState;
        this.setMethodPrefix(persistentState.getMethodPrefix());
        this.setConsiderFinalFieldsOnly(persistentState.isConsiderFinalFieldsOnly());
    }
    
    public JPanel getRootPanel() {
        return this.rootPanel;
    }
    
    public String getMethodPrefix() {
        return this.fieldPrefix.getText();
    }
    
    public void setMethodPrefix(final String prefix) {
        this.fieldPrefix.setText(prefix);
    }
    
    public void setConsiderFinalFieldsOnly(final boolean considerFinalFieldsOnly) {
        this.checkBoxFinalFieldsOnly.setSelected(considerFinalFieldsOnly);
    }
    
    public boolean isModified() {
        return !this.persistentState.getMethodPrefix().equals(this.fieldPrefix.getText()) ||
                this.persistentState.isConsiderFinalFieldsOnly() != this.checkBoxFinalFieldsOnly.isSelected() ||
                this.persistentState.isCreateStaticBuilderMethod() != this.checkBoxCreateStaticBuilderMethod.isSelected();
                
    }
    
    public void reset() {
        this.setMethodPrefix(this.persistentState.getMethodPrefix());
        this.checkBoxFinalFieldsOnly.setSelected(this.persistentState.isConsiderFinalFieldsOnly());
    }
    
    public boolean isConsiderFinalFieldsOnly() {
        return this.checkBoxFinalFieldsOnly.isSelected();
    }
    
    public boolean isCreateStaticBuilderMethod() {
        return this.checkBoxCreateStaticBuilderMethod.isSelected();
    }
}
