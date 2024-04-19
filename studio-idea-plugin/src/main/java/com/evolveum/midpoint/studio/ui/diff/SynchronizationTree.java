package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

public class SynchronizationTree extends Tree {

    public SynchronizationTree(@NotNull SynchronizationTreeModel model) {
        super(model);

        setup();
    }

    private void setup() {
        setRootVisible(false);
    }

    @Override
    public SynchronizationTreeModel getModel() {
        return (SynchronizationTreeModel) super.getModel();
    }
}
