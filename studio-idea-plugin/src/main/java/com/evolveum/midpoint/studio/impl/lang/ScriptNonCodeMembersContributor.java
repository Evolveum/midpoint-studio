package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathHolder;
import com.evolveum.midpoint.prism.path.NameItemPathSegment;
import com.evolveum.midpoint.prism.path.UniformItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.GrDynamicImplicitProperty;
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtilKt;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ScriptNonCodeMembersContributor extends NonCodeMembersContributor {

    private static final Logger LOG = Logger.getInstance(ScriptNonCodeMembersContributor.class);

    @Override
    public void processDynamicElements(@NotNull PsiType qualifierType, PsiClass aClass,
                                       @NotNull PsiScopeProcessor processor, @NotNull PsiElement place,
                                       @NotNull ResolveState state) {
        if (aClass == null) {
            return;
        }

        PsiClass parent = aClass.getSuperClass();
        if (parent == null) {// || !Script.class.getName().equals(parent.getQualifiedName())) {
            return;
        }

        // we want to create member properties only for parent script

        boolean shouldProcessProperties = ResolveUtilKt.shouldProcessProperties(processor);
        if (!shouldProcessProperties) {
            return;
        }

        PsiManager psiManager = PsiManager.getInstance(aClass.getProject());

        for (MidPointExpressionVariables v : MidPointExpressionVariables.values()) {
            Class type = getVariableType(v);

            PsiVariable variable = new GrDynamicImplicitProperty(psiManager, v.getVariable(),
                    type.getName(), aClass.getQualifiedName());

            if (!ResolveUtil.processElement(processor, variable, state)) {
                return;
            }
        }

        addMappingSources(qualifierType, aClass, processor, place, state);
    }

    private Class getVariableType(MidPointExpressionVariables var) {
        if (var.getType() != null) {
            return var.getType();
        }

        return Object.class;    // todo improve for "input" variable and/or variables that can contain subtypes
    }

    private void addMappingSources(PsiType qualifierType, PsiClass aClass, PsiScopeProcessor processor, PsiElement place, ResolveState state) {
        XmlTag script = findScriptCodeTag(place);
        if (script == null || script.getParentTag() == null) {
            return;
        }

        XmlTag parent = script.getParentTag();
        if ("expression".equals(parent.getLocalName())) {
            parent = parent.getParentTag();
        }
        if (parent == null) {
            return;
        }

        XmlTag[] sources = parent.findSubTags("source", SchemaConstantsGenerated.NS_COMMON);

        PsiManager psiManager = PsiManager.getInstance(aClass.getProject());

        for (XmlTag source : sources) {
            XmlTag pathTag = MidPointUtils.findSubTag(source, SchemaConstantsGenerated.C_PATH);
            XmlTag nameTag = MidPointUtils.findSubTag(source, SchemaConstants.C_NAME);

            List<QName> namePath = new ArrayList<>();
            if (pathTag != null && pathTag.getValue() != null) {
                namePath = parseNamePath(pathTag.getValue().getText());
            }

            String name = null;
            if (!namePath.isEmpty()) {
                name = namePath.get(namePath.size() - 1).getLocalPart();
            }

            if (nameTag != null && nameTag.getValue() != null && nameTag.getValue().getText() != null) {
                name = nameTag.getValue().getText();
            }

            if (name == null) {
                // we don't have variable name yet
                continue;
            }

            Class type = Object.class;
            if (!namePath.isEmpty()) {
                type = getSourceVariableType(namePath);
            }

            PsiVariable var = new GrDynamicImplicitProperty(psiManager, name, type.getName(), aClass.getQualifiedName());
            if (!ResolveUtil.processElement(processor, var, state)) {
                break;
            }
        }
    }

    private List<QName> parseNamePath(String path) {
        List<QName> names = new ArrayList<>();
        if (path == null) {
            return names;
        }

        try {
            UniformItemPath itemPath = ItemPathHolder.parseFromString(path);
            itemPath = itemPath.namedSegmentsOnly();

            itemPath.getSegments().stream().forEach(s -> names.add(((NameItemPathSegment) s).getName()));
        } catch (Exception ex) {
        }

        return names;
    }

    private Class getSourceVariableType(List<QName> names) {
        if (names == null || names.isEmpty()) {
            return Object.class;
        }

        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        SchemaRegistry registry = ctx.getSchemaRegistry();

        List<ItemDefinition> results = new ArrayList<>();

        final Class[] FOCUSES = {
                UserType.class,
                RoleType.class,
                OrgType.class,
                ServiceType.class,
                ArchetypeType.class,
                GenericObjectType.class};

        for (Class<? extends Objectable> c : FOCUSES) {
            try {
                ItemDefinition def = registry.findItemDefinitionByFullPath(c, ItemDefinition.class, names.toArray(new QName[names.size()]));
                if (def != null) {
                    results.add(def);
                }
            } catch (Exception ex) {
                // nothing to do
            }
        }

        for (ItemDefinition def : results) {
            if (def.getTypeClassIfKnown() != null) {
                return def.getTypeClassIfKnown();
            }
        }

        return Object.class;
    }

    private XmlTag findScriptCodeTag(PsiElement place) {
        PsiElement e = place;

        XmlTag script = null;
        for (int i = 0; i < 20; i++) {
            if (e == null) {
                break;
            }

            if (e instanceof XmlTag) {
                XmlTag tag = (XmlTag) e;
                if ("script".equals(tag.getLocalName()) && SchemaConstantsGenerated.NS_COMMON.equals(tag.getNamespace())) {
                    script = tag;
                    break;
                }
            }

            e = e.getContext();
        }

        return script;
    }
}
