package net.objecthunter.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

public class GenerateBuilderAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final PsiFile file = anActionEvent.getData(DataKeys.PSI_FILE);
        final Editor editor = anActionEvent.getData(DataKeys.EDITOR);

        if (file == null || editor == null) {
            return;
        }

        final int offset = editor.getCaretModel().getOffset();
        final PsiElement element = file.findElementAt(offset);
        final PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class);

        new WriteCommandAction.Simple(clazz.getProject(), clazz.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                final BuilderCodeGenerator generator = new BuilderCodeGenerator(clazz, BuilderPersistentState.getInstance(anActionEvent.getProject()));
                generator.generateBuilder();
            }
        }.execute();
    }
}
