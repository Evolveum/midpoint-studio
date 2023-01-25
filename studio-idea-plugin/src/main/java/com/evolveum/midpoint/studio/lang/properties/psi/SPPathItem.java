package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.StudioPropertiesTokenTypes;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPPathItem extends LeafPsiElement {

    public SPPathItem(@NotNull CharSequence text) {
        super(StudioPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(StudioPropertiesParser.RULE_pathItem), text);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }
}
