package com.evolveum.midpoint.studio.impl.lang.xnode;

import com.evolveum.axiom.lang.antlr.TokenCustom;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;
import com.evolveum.midpoint.prism.impl.query.lang.Filter;
import com.evolveum.midpoint.prism.query.Suggestion;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.JsonToXNode;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.XNodeConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.XmlToXNode;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.YamlToXNode;
import com.evolveum.midpoint.studio.lang.CompletionContributorBase;
import com.evolveum.midpoint.util.SingleLocalizableMessage;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import java.util.*;

/**
 * Created by Dominik.
 */
public class XNodeContributor extends CompletionContributorBase {

    public XNodeContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {

                        PsiElement positionElement = parameters.getPosition();

                        XNodeConverter xNodeConverterImpl = null;
                        Language lang = positionElement.getLanguage();

                        if (lang.equals(XMLLanguage.INSTANCE)) {
                            xNodeConverterImpl = new XmlToXNode();
                        } else if (lang.equals(JsonLanguage.INSTANCE)) {
                            xNodeConverterImpl = new JsonToXNode();
                        } else if (lang.equals(YAMLLanguage.INSTANCE)) {
                            xNodeConverterImpl = new YamlToXNode();
                        } else {
                            return;
                        }

                        try {
                            // TODO thinking about how to get definition in place of ths cursor from External Annotator
                            XNode xNode = xNodeConverterImpl.convertFromPsi(positionElement);
                            assert xNode != null;
                            generateSuggestion(xNode.getDefinition()).forEach(suggest -> {
                                result.addElement(build(suggest));
                            });
                        } catch (SchemaException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private List<String> generateSuggestion(ItemDefinition<?> itemDefinition) {

        List<String> suggestion = new ArrayList<>();

        if (itemDefinition instanceof PrismContainerDefinition<?> containerDefinition) {
            String parentPath = "";

            containerDefinition.getDefinitions().forEach(prop -> {
                suggestion.add(parentPath + prop.getItemName().getLocalPart());

                if (prop instanceof PrismContainerDefinition<?> containerDefinition1) {
                    containerDefinition1.getDefinitions().forEach( o -> {
                        suggestion.add(parentPath + containerDefinition1.getItemName().getLocalPart() + Filter.Token.SLASH.getName() +
                                o.getItemName().getLocalPart());
                    });
                }
            });
        } else if (itemDefinition instanceof PrismReferenceDefinition prismReferenceDefinition) {
            // TODO to solve target type objects
        }
        else if (itemDefinition instanceof ComplexTypeDefinition complexTypeDefinition) {
            complexTypeDefinition.getDefinitions().forEach(d -> {
                suggestion.add(d.getItemName().getLocalPart());
            });
        }

        return suggestion;
    }

    private LookupElement build(String suggest) {
        // TODO aliases and priority (probably from schema)
        LookupElementBuilder builder = LookupElementBuilder.create(suggest)
                .withLookupStrings(Arrays.asList(suggest, suggest.toLowerCase(), "", ""))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
        return PrioritizedLookupElement.withPriority(element, 0);
    }
}
