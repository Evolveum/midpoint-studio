package com.evolveum.midpoint.studio.lang.properties.psi;

import com.evolveum.midpoint.studio.lang.properties.StudioPropertiesTokenTypes;
import com.evolveum.midpoint.studio.lang.properties.antlr.StudioPropertiesParser;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPPathItem extends LeafPsiElement {

    private FileReference reference = new FileReference();

    public SPPathItem(@NotNull CharSequence text) {
        super(StudioPropertiesTokenTypes.RULE_ELEMENT_TYPES.get(StudioPropertiesParser.RULE_pathItem), text);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }

    private String getFileName() {
        return getText();
    }

    @Override
    public @NotNull Collection<? extends @NotNull PsiSymbolReference> getOwnReferences() {
//        return Collections.singletonList(PsiSymbolService.getInstance().asSymbolReference(myReference));
//
//        List<? extends @NotNull PsiSymbolReference> refs = new ArrayList<>();
//
//        VfsUtil.
//                FileReferenceUtil.findFile()
//
//
//        VirtualFile file = getContainingFile().getVirtualFile();
//        new FileReference(file) l;
////        VirtualFileManagerEx.getInstance().
////file.findChild("..")
//
//        return refs;
        return Collections.emptyList();
    }
}
