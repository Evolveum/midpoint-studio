package com.evolveum.midpoint.studio.ui.structureView;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFileTreeModel extends TextEditorBasedStructureViewModel
        implements StructureViewModel.ElementInfoProvider {

    public MidPointFileTreeModel(Editor editor, XmlFile file) {
        super(editor, file);
    }

    @Override
    protected XmlFile getPsiFile() {
        return (XmlFile) super.getPsiFile();
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return false;
    }

    @NotNull
    @Override
    public StructureViewTreeElement getRoot() {
        return new FileTreeElement(getPsiFile());
    }
}
