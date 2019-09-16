package com.evolveum.midpoint.studio.impl.lang;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlNamespaceHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointNamespaceHelper extends XmlNamespaceHelper {

    @Override
    protected boolean isAvailable(PsiFile file) {
        return true;
    }

    @Override
    public void insertNamespaceDeclaration(@NotNull XmlFile file, @Nullable Editor editor,
                                           @NotNull Set<String> possibleNamespaces, @Nullable String nsPrefix,
                                           @Nullable Runner<String, IncorrectOperationException> runAfter)
            throws IncorrectOperationException {


    }

    @NotNull
    @Override
    public Set<String> guessUnboundNamespaces(@NotNull PsiElement element, XmlFile file) {
        if (!(element instanceof XmlTag)) {
            return Collections.emptySet();
        }

        // todo implement

        return new HashSet<>(Arrays.asList("http://midpoint.evolveum.com/xml/ns/public/common/common-3"));
    }

    @NotNull
    @Override
    public Set<String> getNamespacesByTagName(@NotNull String tagName, @NotNull XmlFile context) {
        // todo implement

        return new HashSet<>(Arrays.asList("http://midpoint.evolveum.com/xml/ns/public/common/common-3"));
    }
}
