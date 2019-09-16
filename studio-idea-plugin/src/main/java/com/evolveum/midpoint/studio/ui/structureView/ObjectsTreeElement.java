package com.evolveum.midpoint.studio.ui.structureView;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectsTreeElement extends MidPointPsiTreeElement<XmlTag> {

    public ObjectsTreeElement(XmlTag psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        List<StructureViewTreeElement> result = new ArrayList<>();

        for (XmlTag e : getElement().getSubTags()) {
            result.add(new ObjectTreeElement(e));
        }

        return result;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return "Objects";
    }
}
