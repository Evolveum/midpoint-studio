package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.lang.properties.psi.SPPath;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPPathManipulator extends AbstractElementManipulator<SPPath> {

    @Override
    public @Nullable SPPath handleContentChange(
            @NotNull SPPath element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {

        CheckUtil.checkWritable(element);

        String text;
        final String oldText = element.getText();

        String textBeforeRange = oldText.substring(0, range.getStartOffset());
        String textAfterRange = oldText.substring(range.getEndOffset());
        newContent = oldText.startsWith("'") || oldText.endsWith("'") ?
                newContent.replace("'", oldText.contains("&#39;") ? "&#39;" : "&apos;") :
                newContent.replace("\"", oldText.contains("&#34;") ? "&#34;" : "&quot;");
        text = "<a value=" + textBeforeRange + newContent + textAfterRange;

        final Project project = element.getProject();
        final XmlTag tag = element.getParent().getParent() instanceof HtmlTag ?
                XmlElementFactory.getInstance(project).createHTMLTagFromText(text) :
                XmlElementFactory.getInstance(project).createTagFromText(text);
        final XmlAttribute attribute = tag.getAttribute("value");
        assert attribute != null && attribute.getValueElement() != null;
        element.getNode().replaceAllChildrenToChildrenOf(attribute.getValueElement().getNode());
        return element;
    }

    @Override
    @NotNull
    public TextRange getRangeInElement(@NotNull final SPPath path) {
        final PsiElement first = path.getFirstChild();
        if (first == null) {
            return TextRange.EMPTY_RANGE;
        }
        final ASTNode firstNode = first.getNode();
        assert firstNode != null;
        final PsiElement last = path.getLastChild();
        final ASTNode lastNode = last != null && last != first ? last.getNode() : null;

        final int textLength = path.getTextLength();
        final int start = firstNode.getElementType() == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER ? first.getTextLength() : 0;
        final int end = lastNode != null && lastNode.getElementType() == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER ? last.getTextLength() : 0;
        return new TextRange(start, textLength - end);
    }
}
