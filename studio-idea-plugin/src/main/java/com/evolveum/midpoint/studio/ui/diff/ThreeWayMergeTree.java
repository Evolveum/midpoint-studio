package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.Disposable;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

public class ThreeWayMergeTree extends Tree implements Disposable {

    public ThreeWayMergeTree(ThreeWayMergeTreeModel model) {
        super(model);

        setup();
    }

    @Override
    public void dispose() {
        UIUtil.dispose(this);
    }

    private void setup() {
        setRootVisible(false);

    }
}
