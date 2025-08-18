package com.evolveum.midpoint.studio.impl.lang.xnode.contributor;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismPropertyDefinitionImpl;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiToJsonConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiToXmlConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.PsiToYamlConverter;
import com.evolveum.midpoint.studio.lang.CompletionContributorBase;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import javax.xml.namespace.QName;
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
                        Language language = positionElement.getLanguage();
                        PsiConverter psiConverter = getPsiConverter(language);
                        PsiElement parent = positionElement.getParent();
                        Stack<String> parentElements = new Stack<>();

                        while (!(parent instanceof PsiFile)) {
                            String element = psiConverter.convert(parent, false);

                            // FIXME remove item with caret marker IntellijIdeaRulezzz. not to rely on on caret marker
                            if (element != null && !element.isEmpty() && !element.contains("IntellijIdeaRulezzz")) {
                                parentElements.push(element);
                            }

                            parent = parent.getParent();
                        }

                        try {
                            PrismContext prismContext = StudioPrismContextService.getPrismContext(positionElement.getProject());
                            ParsingContext parsingCtx = prismContext.createParsingContextForCompatibilityMode();
                            ItemDefinition<?> definition = null;

                            if (parentElements.isEmpty()) {
                                initGenSuggestion(prismContext).forEach(suggest -> {
                                    result.addElement(build(suggest, language));
                                });
                            } else {
                                // find definition of caret place for generate suggestion
                                while (!parentElements.isEmpty()) {
                                    String value = parentElements.pop();

                                    RootXNode root = prismContext.parserFor(value)
                                            .language(language.getDisplayName().toLowerCase())
                                            .context(parsingCtx)
                                            .definition(definition)
                                            .parseToXNode();

                                    definition = root.getSubnode().getDefinition();
                                }

                                generateSuggestion(prismContext, definition).forEach(suggest -> {
                                    result.addElement(build(suggest, language));
                                });
                            }
                        } catch (SchemaException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    private @NotNull PsiConverter getPsiConverter(Language language) {
                        if (language.equals(XMLLanguage.INSTANCE)) {
                            return new PsiToXmlConverter();
                        } else if (language.equals(JsonLanguage.INSTANCE)) {
                            return new PsiToJsonConverter();
                        } else if (language.equals(YAMLLanguage.INSTANCE)) {
                            return new PsiToYamlConverter();
                        }

                        throw new IllegalArgumentException("Unsupported element type of language: " + language);
                    }
                });
    }

    private List<SuggestPair> generateSuggestion(PrismContext prismContext, ItemDefinition<?> itemDefinition) {

        List<SuggestPair> suggestion = new ArrayList<>();

        if (itemDefinition instanceof PrismContainerDefinition<?> containerDefinition) {
            containerDefinition.getDefinitions().forEach(d -> {
                suggestion.add(new SuggestPair(d.getItemName(), d.getClass()));
            });
        } else if (itemDefinition instanceof PrismReferenceDefinition prismReferenceDefinition) {
            PrismObjectDefinition<?> targetDef = prismContext.getSchemaRegistry()
                    .findObjectDefinitionByType(prismReferenceDefinition.getTargetTypeName());

            if (targetDef != null) {
                generateSuggestion(prismContext, targetDef);
            }
        } else if (itemDefinition instanceof ComplexTypeDefinition complexTypeDefinition) {
            complexTypeDefinition.getDefinitions().forEach(d -> {
                suggestion.add(new SuggestPair(d.getItemName(), d.getClass()));
            });
        }

        return suggestion;
    }

    private List<SuggestPair> initGenSuggestion(PrismContext prismContext) {
        List<SuggestPair> suggestion = new ArrayList<>();

        prismContext.getSchemaRegistry().getSchemas().forEach(prismSchema -> {
            prismSchema.getObjectDefinitions().forEach(objectDefinition -> {
                suggestion.add(new SuggestPair(objectDefinition.getItemName(), objectDefinition.getClass()));
            });
        });

        return suggestion;
    }

    private LookupElement build(SuggestPair suggest, Language language) {
        // TODO aliases and priority (probably from schema)
        LookupElementBuilder builder = LookupElementBuilder.create(suggest.itemName.getLocalPart())
                .withInsertHandler((insertionContext, item) -> {
                        if (language.equals(XMLLanguage.INSTANCE)) {
                            String tagText =
                                    "<" + item.getLookupString() + "></" + item.getLookupString() + ">";
                            int startOffset = insertionContext.getStartOffset();

                            insertionContext.getDocument().replaceString(
                                    startOffset,
                                    insertionContext.getTailOffset(),
                                    tagText
                            );

                            insertionContext.getEditor().getCaretModel().moveToOffset(
                                    startOffset + item.getLookupString().length() +2
                            );
                        } else if (language.equals(JsonLanguage.INSTANCE)) {
                            String tagText = "\"" + item.getLookupString() + "\": " +
                                    ((suggest.type.equals(PrismPropertyDefinitionImpl.class)) ? "\"\"" : "{}");
                            int startOffset = insertionContext.getStartOffset();

                            insertionContext.getDocument().replaceString(
                                    startOffset,
                                    insertionContext.getTailOffset(),
                                    tagText
                            );

                            insertionContext.getEditor().getCaretModel().moveToOffset(
                                    startOffset + item.getLookupString().length() +5
                            );
                        } else if (language.equals(YAMLLanguage.INSTANCE)) {
                            String tagText = "" + item.getLookupString() + ": ";
                            int startOffset = insertionContext.getStartOffset();

                            insertionContext.getDocument().replaceString(
                                    startOffset,
                                    insertionContext.getTailOffset(),
                                    tagText
                            );

                            insertionContext.getEditor().getCaretModel().moveToOffset(
                                    startOffset + item.getLookupString().length() + 2
                            );
                        }
                })
                .withLookupStrings(Arrays.asList(suggest.itemName.getLocalPart(),
                        suggest.itemName.getLocalPart().toLowerCase(), "", ""))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
        return PrioritizedLookupElement.withPriority(element, 0);
    }

    record SuggestPair (
            QName itemName,
            Class<?> type
    ){}
}
