package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.InitialObjectsCache;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.impl.psi.search.OidNameValue;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OidInlayParameterHintsProvider implements InlayParameterHintsProvider {

    @NotNull
    @Override
    public List<InlayInfo> getParameterHints(PsiElement element) {
        if (!PsiUtils.isReferenceOidAttributeValue(element) && !PsiUtils.isReferenceOidTag(element)) {
            return Collections.emptyList();
        }

        XmlTag ref = PsiUtils.findObjectReferenceTag(element);
        if (ref == null) {
            return Collections.emptyList();
        }

        String oid = PsiUtils.getOidFromReferenceTag(ref);
        if (oid == null) {
            return Collections.emptyList();
        }

        int offset = inlayOffset(element, false);
        List<OidNameValue> result = ObjectFileBasedIndexImpl.getOidNamesByOid(oid, element.getProject());
        if (result.isEmpty()) {
            return createInitialObjectInlay(element.getProject(), oid, offset);
        }

        String label;
        if (result.size() == 1) {
            label = result.iterator().next().getName();
            if (StringUtils.isEmpty(label)) {
                label = "Name undefined";
            }
        } else {
            boolean multiple = false;

            label = result.iterator().next().getName();
            for (OidNameValue o : result) {
                if (!Objects.equals(label, o.getName())) {
                    multiple = true;
                    break;
                }
            }

            if (multiple) {
                label = "Multiple object with same oid";
            } else {
                label = label + " (multiple objects)";
            }
        }

        return List.of(new InlayInfo(label, offset, false, true, false));
    }

    private List<InlayInfo> createInitialObjectInlay(Project project, String oid, int offset) {
        InitialObjectsCache cache = EnvironmentCacheManager.getCache(project, EnvironmentCacheManager.KEY_INITIAL_OBJECTS);
        if (cache == null) {
            return Collections.emptyList();
        }

        ObjectType object = cache.get(oid);
        if (object == null) {
            return Collections.emptyList();
        }

        String name = MidPointUtils.getDisplayNameOrName(object);
        if (name == null) {
            name = "Undefined";
        }

        return List.of(new InlayInfo(name, offset, false, true, false));
    }

    private int inlayOffset(PsiElement expr, boolean atEnd) {
        if (expr.getTextRange().isEmpty()) {
            PsiElement next = expr.getNextSibling();
            if (next instanceof PsiWhiteSpace) {
                return next.getTextRange().getEndOffset();
            }
        }

        if (atEnd) {
            return expr.getTextRange().getEndOffset();
        }

        return expr.getTextRange().getStartOffset();
    }


    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return Collections.emptySet();
    }
}
