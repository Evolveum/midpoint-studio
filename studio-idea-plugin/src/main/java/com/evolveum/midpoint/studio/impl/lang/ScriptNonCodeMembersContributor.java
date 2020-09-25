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
import org.apache.commons.lang3.StringUtils;
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
        addFunctionParameters(qualifierType, aClass, processor, place, state);
    }

    private Class getVariableType(MidPointExpressionVariables var) {
        if (var.getType() != null) {
            return var.getType();
        }

        return Object.class;    // todo improve for "input" variable and/or variables that can contain subtypes
    }

    private void addFunctionParameters(PsiType qualifierType, PsiClass aClass, PsiScopeProcessor processor, PsiElement place, ResolveState state) {
        XmlTag function = findParentTag(place, SchemaConstantsGenerated.C_FUNCTION);
        if (function == null) {
            return;
        }

        XmlTag[] parameters = function.findSubTags(ExpressionType.F_PARAMETER.getLocalPart(), SchemaConstantsGenerated.NS_COMMON);

        addParameters(parameters, aClass, processor, state);
    }

    private void addParameters(XmlTag[] parameters, PsiClass aClass, PsiScopeProcessor processor, ResolveState state) {
        PsiManager psiManager = PsiManager.getInstance(aClass.getProject());

        for (XmlTag parameter : parameters) {
            XmlTag nameTag = MidPointUtils.findSubTag(parameter, ExpressionParameterType.F_NAME.asSingleName());
            XmlTag typeTag = MidPointUtils.findSubTag(parameter, ExpressionParameterType.F_TYPE.asSingleName());

            String name = null;
            if (nameTag != null && nameTag.getValue() != null) {
                name = nameTag.getValue().getText();
            }

            if (StringUtils.isEmpty(name)) {
                continue;
            }

            Class type = Object.class;
            if (typeTag != null && typeTag.getValue() != null) {
                type = getParameterVariableType(typeTag);
            }

            PsiVariable var = new GrDynamicImplicitProperty(psiManager, name, type.getName(), aClass.getQualifiedName());
            if (!ResolveUtil.processElement(processor, var, state)) {
                break;
            }
        }
    }

    private void addMappingSources(PsiType qualifierType, PsiClass aClass, PsiScopeProcessor processor, PsiElement place, ResolveState state) {
        XmlTag expression = findParentTag(place, SchemaConstantsGenerated.C_EXPRESSION);
        if (expression == null) {
            return;
        }

        XmlTag expressionParent = expression.getParentTag();
        if (expressionParent == null) {
            return;
        }

        XmlTag[] sources = expressionParent.findSubTags(MappingType.F_SOURCE.getLocalPart(), SchemaConstantsGenerated.NS_COMMON);
        addSources(sources, aClass, processor, state);

        XmlTag[] parameters = expression.findSubTags(ExpressionType.F_PARAMETER.getLocalPart(), SchemaConstantsGenerated.NS_COMMON);

        addParameters(parameters, aClass, processor, state);
    }

    private void addSources(XmlTag[] sources, PsiClass aClass, PsiScopeProcessor processor, ResolveState state) {
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

    private Class getParameterVariableType(XmlTag typeTag) {
        if (typeTag == null || typeTag.getValue() == null) {
            return Object.class;
        }

        String type = typeTag.getValue().getText();
        if (StringUtils.isEmpty(type)) {
            return Object.class;
        }

        String[] split = type.split(":", -1);
        String localPart;
        String namespace;
        if (split.length > 1) {
            localPart = split[1];
            namespace = typeTag.getNamespaceByPrefix(split[0]);
        } else {
            localPart = split[0];
            namespace = typeTag.getNamespace();
        }

        QName qname = new QName(namespace, localPart);

        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        SchemaRegistry registry = ctx.getSchemaRegistry();

        return registry.determineClassForType(qname);
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

    private XmlTag findParentTag(PsiElement element, QName parentTagName) {
        XmlTag parent = null;
        for (int i = 0; i < 20; i++) {
            if (element == null) {
                break;
            }

            if (element instanceof XmlTag) {
                XmlTag tag = (XmlTag) element;
                if (parentTagName.getLocalPart().equals(tag.getLocalName()) && parentTagName.getNamespaceURI().equals(tag.getNamespace())) {
                    parent = tag;
                    break;
                }
            }

            element = element.getContext();
        }

        return parent;
    }
}
