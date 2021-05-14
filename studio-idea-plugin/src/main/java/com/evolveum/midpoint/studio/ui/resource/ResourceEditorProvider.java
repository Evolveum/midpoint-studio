package com.evolveum.midpoint.studio.ui.resource;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ResourceEditorProvider implements FileEditorProvider {

    private static final String EDITOR_TYPE_ID = "resource-editor-ui";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (project == null || file == null) {
            return false;
        }

        if (!"xml".equalsIgnoreCase(file.getExtension())) {
            return false;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }

        XmlFile xmlFile = (XmlFile) psiFile;
        XmlTag root = xmlFile.getRootTag();
        if (root == null) {
            return false;
        }

        QName qname = MidPointUtils.createQName(root);

        return ObjectTypes.RESOURCE.getElementName().equals(qname);
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new ResourceEditor(project, file);
    }

    @Override
    public @NotNull
    @NonNls
    String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
