package com.evolveum.midpoint.studio.ui.delta;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.impl.xml.ObjectsDiffFactory;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaObjectType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
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
public class ObjectDeltaEditorProvider implements FileEditorProvider, DumbAware {

    private static final String EDITOR_TYPE_ID = "object-delta-ui";

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

        QName rootName = MidPointUtils.createQName(root);
        QName rootXsiType = MidPointUtils.elementXsiType(root);

        return SchemaConstantsGenerated.T_OBJECT_DELTA_OBJECT.equals(rootName) ||
                ObjectDeltaObjectType.COMPLEX_TYPE.equals(rootXsiType) ||
                ObjectsDiffFactory.Q_DIFF.equals(rootName);
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new ObjectDeltaEditor(project, file);
    }

    @Override
    public @NotNull
    @NonNls
    String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
    }
}
