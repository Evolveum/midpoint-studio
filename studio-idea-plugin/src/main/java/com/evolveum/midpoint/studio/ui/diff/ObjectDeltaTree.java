package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

public class ObjectDeltaTree<O extends ObjectType> extends Tree {

    public ObjectDeltaTree(@NotNull ObjectDeltaTreeModel<O> model) {
        super(model);  // todo implement
    }
}
