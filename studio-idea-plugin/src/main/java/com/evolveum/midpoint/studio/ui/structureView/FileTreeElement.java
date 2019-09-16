package com.evolveum.midpoint.studio.ui.structureView;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Viliam Repan (lazyman).
 */
public class FileTreeElement extends MidPointPsiTreeElement<XmlFile> {

    public FileTreeElement(XmlFile psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        XmlTag root = getElement().getRootTag();
        if (root == null) {
            return new ArrayList<>();
        }

        if ("objects".equals(root.getName())) {
            return Arrays.asList(new ObjectsTreeElement(root));
        } else if (isMidPointObject(root)) {
            return Arrays.asList(new ObjectTreeElement(root));
        }

        return new ArrayList<>();
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return "File: " + getElement().getName();
    }

    private boolean isMidPointObject(XmlTag tag) {
        QName name = new QName(tag.getNamespace(), tag.getName());

        for (ObjectTypes t : ObjectTypes.values()) {
            if (t.getElementName().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
