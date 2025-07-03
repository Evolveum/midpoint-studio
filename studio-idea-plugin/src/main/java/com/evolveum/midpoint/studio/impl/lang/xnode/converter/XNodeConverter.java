package com.evolveum.midpoint.studio.impl.lang.xnode.converter;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeFactoryImpl;
import com.evolveum.midpoint.prism.xnode.Position;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.prism.xnode.XNodeFactory;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface XNodeConverter {

    XNodeFactory xNodeFactory = new XNodeFactoryImpl();

    @Nullable
    XNode convertFromPsi(PsiElement element) throws SchemaException;

    default XNodeDefinition resolveXNodeDefinition(String name, XNodeDefinition schema, PrismNamespaceContext namespaceContext) throws SchemaException {
        return schema.resolve(name, namespaceContext);
    }

    @Nullable
    default Position calculatePosition(PsiElement element) {
        TextRange range = element.getTextRange();
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());

        int startOffset = range.getStartOffset();
        int endOffset = range.getEndOffset();

        if (document != null) {
            int startLine = document.getLineNumber(startOffset);
            int endLine = document.getLineNumber(endOffset);

            int startColumn = startOffset - document.getLineStartOffset(startLine);
            int endColumn = endOffset - document.getLineStartOffset(endLine);

            return new Position(startLine, endLine, startColumn,endColumn);
        } else {
            return null;
        }
    }

}
