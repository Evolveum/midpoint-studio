package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.lang.DiffIgnoredRangeProvider;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointDiffIgnoreRangeProvider implements DiffIgnoredRangeProvider {

    private static final Set<ItemPath> OPERATIONAL = new HashSet<>();

    private static final Set<ItemPath> NON_OPERATIONAL = new HashSet<>();

    private static final Set<String> IGNORED_ATTRIBUTES = Set.of("id");

    private static boolean isOperational(@NotNull Project project, @NotNull QName type, @NotNull ItemPath path) {
        // we're not interested in container IDs when searching for definitions
        path = path.namedSegmentsOnly();

        ItemName name = path.lastName();
        if (name != null && name.getLocalPart().startsWith("_")) {
            if (name.getLocalPart().equals("_value")) {
                path = path.allExceptLast();
            } else {
                // metadata
                return true;
            }
        }

        if (OPERATIONAL.contains(path)) {
            return true;
        }

        if (NON_OPERATIONAL.contains(path)) {
            return false;
        }

        var typeClass = ObjectTypes.getObjectTypeClassIfKnown(type);
        if (typeClass == null) {
            return false;
        }

        var ctx = StudioPrismContextService.getPrismContext(project);
        var objectDef = ctx.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(typeClass);
        if (objectDef == null) {
            return false;
        }

        var itemDef = objectDef.findItemDefinition(path);
        if (itemDef == null) {
            return false;
        }

        boolean operational = itemDef.isOperational();

        if (operational) {
            OPERATIONAL.add(path);
        } else {
            NON_OPERATIONAL.add(path);
        }

        return operational;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Ignores midPoint metadata, operational data";
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
            if (project == null) {
                return result;
            }

            PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("", XMLLanguage.INSTANCE, text);

            psiFile.accept(new PsiElementVisitor() {

                @Override
                public void visitElement(@NotNull PsiElement element) {
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
        if (element instanceof PsiWhiteSpace) {
            return true;
        }

        if (element instanceof XmlAttribute attr && IGNORED_ATTRIBUTES.contains(attr.getLocalName())) {
            return true;
        }

        if (!(element instanceof XmlTag tag)) {
            return false;
        }

        XmlFile xmlFile = (XmlFile) tag.getContainingFile();
        XmlTag rootTag = xmlFile.getRootTag();

        if (rootTag == null) {
            return false;
        }

        QName type = PsiUtils.getTagXsdType(rootTag);
        if (type == null) {
            return false;
        }

        ItemPath path = PsiUtils.createItemPath(tag);

        // item path doesn't start from root element
        return isOperational(element.getProject(), type, path.rest());
    }
}
