package net.objecthunter.idea;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BuilderConfigurationUI {
    
    private static final String PREFIX_HINT = "Builder methods will be called \"{}\"";
    
    private JTextField fieldPrefix;
    
    private JPanel rootPanel;
    
    private JLabel labelMethodPrefixHint;
    
    private BuilderPersistentState persistentState;
    
    public BuilderConfigurationUI(final BuilderPersistentState persistentState) {
        this.persistentState = persistentState;
        this.setMethodPrefix(persistentState.getMethodPrefix());
        this.fieldPrefix.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent documentEvent) {
                updateMethodPrefixHint();
            }
    
            @Override
            public void removeUpdate(final DocumentEvent documentEvent) {
                updateMethodPrefixHint();
            }
    
            @Override
            public void changedUpdate(final DocumentEvent documentEvent) {
                updateMethodPrefixHint();
            }
        });
    }
    
    private void updateMethodPrefixHint() {
        final String prefix = this.fieldPrefix.getText();
        this.labelMethodPrefixHint.setText(PREFIX_HINT.replace("{}", StringUtils.isEmpty(prefix) ? "foo(T t)" : prefix + "Foo(T t)"));
    }
    
    public JPanel getRootPanel() {
        return this.rootPanel;
    }
    
    public String getMethodPrefix() {
        return this.fieldPrefix.getText();
    }
    
    public void setMethodPrefix(final String prefix) {
        this.fieldPrefix.setText(prefix);
        this.updateMethodPrefixHint();
    }
    
    public boolean isModified() {
        return !this.persistentState.getMethodPrefix().equals(this.fieldPrefix.getText());
    }
    
    public void reset() {
        this.setMethodPrefix(this.persistentState.getMethodPrefix());
    }
}
