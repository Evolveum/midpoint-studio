package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.lang.DiffIgnoredRangeProvider;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointDiffIgnoreRangeProvider implements DiffIgnoredRangeProvider {

    private static final Set<QName> IGNORED_ELEMENTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ObjectType.F_METADATA,
            ObjectType.F_OPERATION_EXECUTION,
            ObjectType.F_FETCH_RESULT
//            SchemaConstants.C_OBJECTS
    )));

    private static final Set<String> IGNORED_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "id"
//            "xmlns"
    )));

    @NotNull
    @Override
    public String getDescription() {
        return "Ignores container metadata, operation execution, fetch results and ids";
    }

    @Override
    public boolean accepts(@Nullable Project project, @NotNull DiffContent content) {
        return true;
    }

    @NotNull
    @Override
    public List<TextRange> getIgnoredRanges(@Nullable Project project, @NotNull CharSequence text, @NotNull DiffContent content) {
        return ReadAction.compute(() -> {
            List<TextRange> result = new ArrayList<>();
            PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("", XMLLanguage.INSTANCE, text);

            psiFile.accept(new PsiElementVisitor() {

                @Override
                public void visitElement(PsiElement element) {
                    if (element.getTextLength() == 0) return;

                    if (isIgnored(element)) {
                        result.add(element.getTextRange());
                    } else {
                        element.acceptChildren(this);
                    }
                }
            });
            return result;
        });
    }

    private static boolean isIgnored(@NotNull PsiElement element) {
        if (element instanceof PsiWhiteSpace) return true;

        if (element instanceof XmlAttribute) {
            XmlAttribute attr = (XmlAttribute) element;
            if (IGNORED_ATTRIBUTES.contains(attr.getLocalName())) {
                return true;
            }
        }

        if (element instanceof XmlTag) {
            XmlTag em = (XmlTag) element;
            QName name = MidPointUtils.createQName(em);
            if (IGNORED_ELEMENTS.contains(name)) {
                return true;
            }
        }

        return false;
    }
}
