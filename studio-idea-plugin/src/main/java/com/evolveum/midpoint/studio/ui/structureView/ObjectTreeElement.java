package com.evolveum.midpoint.studio.ui.structureView;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectTreeElement extends MidPointPsiTreeElement<XmlTag> {

    public ObjectTreeElement(XmlTag psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return getElement().getName();
    }
}
