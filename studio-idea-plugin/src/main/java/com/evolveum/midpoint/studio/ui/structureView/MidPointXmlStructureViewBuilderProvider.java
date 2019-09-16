package com.evolveum.midpoint.studio.ui.structureView;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointXmlStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {

    @Nullable
    @Override
    public StructureViewBuilder createStructureViewBuilder(@NotNull XmlFile file) {
        return new TreeBasedStructureViewBuilder() {

            @Override
            @NotNull
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new MidPointFileTreeModel(editor, file);
            }

            @Override
            public boolean isRootNodeShown() {
                return false;
            }
        };
    }
}
