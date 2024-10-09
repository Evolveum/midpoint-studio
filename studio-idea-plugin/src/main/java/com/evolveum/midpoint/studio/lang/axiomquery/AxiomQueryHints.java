package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

public interface AxiomQueryHints {

    /**
     * Value for key should be {@link QName} item type.
     */
    Key<QName> ITEM_TYPE_HINT = new Key<>("itemTypeHint");

    default @Nullable ItemDefinition<?> getObjectDefinitionFromHint(@Nullable Editor editor) {
        if (editor == null || editor.getDocument() == null) {
            return null;
        }

        return getItemDefinitionFromHint(editor.getDocument());
    }

    default @Nullable ItemDefinition<?> getItemDefinitionFromHint(@Nullable UserDataHolder holder, PrismContext context) {
        if (holder == null) {
            return null;
        }

        QName type = holder.getUserData(ITEM_TYPE_HINT);
        if (type == null) {
            return null;
        }

        return context.getSchemaRegistry()
                .findObjectDefinitionByType(type);
    }

    default @Nullable ItemDefinition<?> getItemDefinitionFromHint(@Nullable UserDataHolder holder) {
        return getItemDefinitionFromHint(holder, PrismContext.get());
    }
}
