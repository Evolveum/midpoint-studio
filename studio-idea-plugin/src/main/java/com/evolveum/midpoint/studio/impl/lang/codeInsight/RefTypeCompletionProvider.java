package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.editorActions.TabOutScopesTracker;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.XmlNamespaceHelper;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RefTypeCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final Key<QName> QNAME = new Key<QName>("qname");

    private static final List<LookupElement> TYPES;

    static {
        List<LookupElement> elements = new ArrayList<>();

        for (ObjectTypes type : ObjectTypes.values()) {
            LookupElementBuilder element = LookupElementBuilder.create(type.getTypeQName().getLocalPart())
                    .withCaseSensitivity(false)
                    .withInsertHandler(new TypeInsertHandler());

            element.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
            element.putUserData(QNAME, type.getTypeQName());

            elements.add(PrioritizedLookupElement.withPriority(element, 100));
        }

        Collections.sort(elements, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getLookupString(), o2.getLookupString()));

        TYPES = Collections.unmodifiableList(elements);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        result.addAllElements(TYPES);
    }

    private static class TypeInsertHandler implements InsertHandler<LookupElement> {

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            QName qname = item.getUserData(QNAME);

            Editor editor = context.getEditor();

            Document document = editor.getDocument();
            int caretOffset = editor.getCaretModel().getOffset();
            XmlFile file = (XmlFile) context.getFile();

            PsiElement element = file.findElementAt(context.getStartOffset());
            XmlTag tag = element != null ? PsiTreeUtil.getParentOfType(element, XmlTag.class) : null;

            if (tag == null) {
                return;
            }

            String prefix = "c";
            prefix = makePrefixUnique(prefix, tag);

            Project project = context.getProject();
            PsiDocumentManager.getInstance(project).commitDocument(document);

            String value = prefix + ":" + qname.getLocalPart();

            XmlNamespaceHelper helper = XmlNamespaceHelper.getHelper(context.getFile());
            helper.insertNamespaceDeclaration(
                    file, editor, Collections.singleton(qname.getNamespaceURI()), prefix, null);

            TabOutScopesTracker.getInstance().registerEmptyScopeAtCaret(context.getEditor());

//			System.out.println(value);

//			context.setAddCompletionChar(false);
        }

        private static String makePrefixUnique(@NotNull String basePrefix, @NotNull XmlTag context) {
            if (context.getNamespaceByPrefix(basePrefix).isEmpty()) {
                return basePrefix;
            }
            int i = 1;

            while (!context.getNamespaceByPrefix(basePrefix + i).isEmpty()) {
                i++;
            }
            return basePrefix + i;
        }
    }
}
