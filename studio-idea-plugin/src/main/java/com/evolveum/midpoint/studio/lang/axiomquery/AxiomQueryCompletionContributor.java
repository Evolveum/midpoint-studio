package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.axiom.lang.antlr.AxiomStrings;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryContentAssistImpl;
import com.evolveum.midpoint.prism.impl.query.lang.Filter;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.AxiomQueryContentAssist;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.lang.CompletionContributorBase;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryCompletionContributor extends CompletionContributorBase implements AxiomQueryHints {

    private static final Logger LOG = Logger.getInstance(AxiomQueryCompletionContributor.class);

    public AxiomQueryCompletionContributor() {
        extend(null,
                PlatformPatterns.psiElement(),
                new CompletionProvider<>() {

                    @Override
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        StudioPrismContextService.runWithProject(
                                parameters.getPosition().getProject(),
                                () -> AxiomQueryCompletionContributor.this.addCompletions(parameters, resultSet));
                    }
                }
        );
    }

    private void addCompletions(CompletionParameters parameters, CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        String content = parameters.getOriginalFile().getText();

        var prefixElement = findItemNameElement(element);
        if (prefixElement != null) {
            String prefix = prefixElement.getText().replace(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED, "");

            if (prefix.startsWith(AxiomStrings.fromOptionallySingleQuoted(AxiomQueryParser.VOCABULARY.getDisplayName(AxiomQueryParser.AT_SIGN)))) {
                resultSet = resultSet.withPrefixMatcher(new PlainPrefixMatcher(prefix));
            }
        }

        var cursorPosition = parameters.getOffset();
        PsiElement outer = PsiUtils.getOuterPsiElement(element);
        if (!(outer instanceof XmlText xmlText)) {
            addSuggestionsFromEditorHint(parameters.getEditor(), content, cursorPosition, resultSet);
            return;
        }

        // we have axiom query embedded in xml
        XmlTag tag = xmlText.getParentTag();
        if (tag == null) {
            addSuggestionsFromEditorHint(parameters.getEditor(), content, cursorPosition, resultSet);
            return;
        }

        PrismValue value = null;
        try {
            value = getPrismValue(tag);
        } catch (Exception ex) {
            LOG.trace("Couldn't get prism value from xml tag, reason: " + ex.getMessage());
        }

        if (value == null) {
            addSuggestionsFromEditorHint(parameters.getEditor(), content, cursorPosition, resultSet);
            return;
        }

        SchemaContext schemaContext = value.getSchemaContext();
        if (schemaContext == null) {
            addSuggestionsFromEditorHint(parameters.getEditor(), content, cursorPosition, resultSet);
            return;
        }

        ItemDefinition<?> def = schemaContext.getItemDefinition();
        if (def == null) {
            addSuggestionsFromEditorHint(parameters.getEditor(), content, cursorPosition, resultSet);
            return;
        }

        addSuggestions(def, content, cursorPosition, resultSet);
        resultSet.runRemainingContributors(parameters, true);
    }

    private PrismValue getPrismValue(XmlTag tag) {
        if (tag == null) {
            return null;
        }

        List<XmlTag> parentTags = getParentTags(tag);
        parentTags = getObjectFullPath(parentTags);

        if (parentTags.isEmpty()) {
            return null;
        }

        XmlTag objectTag = parentTags.get(0);
        PrismObject<?> object = parseObject(objectTag);

        // rest of the path
        List<XmlTag> tagItemPath = parentTags.subList(1, parentTags.size());

        // this item path might be too long -> it's created from xml tags, might point to complex property value, etc.
        return findValue(object.getValue(), tagItemPath, object.getValue());
    }

    private PrismValue findValue(PrismContainerValue container, List<XmlTag> path, PrismValue lastFound) {
        if (container == null || path.isEmpty()) {
            return lastFound;
        }

        XmlTag tag = path.get(0);

        String idStr = tag.getAttributeValue("id");
        Long id = idStr != null ? Long.parseLong(idStr) : null;

        QName name = MidPointUtils.createQName(tag);

        Item<?, ?> item = container.findItem(ItemPath.create(name));
        if (item instanceof PrismContainer<?> pc) {
            // todo what if id is null but it's multi-value
            PrismContainerValue pcv = id != null ? pc.findValue(id) : pc.getValue();
            return findValue(pcv, path.subList(1, path.size()), pcv);
        } else if (item != null) {
            return item.getValue();
        }

        return lastFound;
    }

    private PrismObject<?> parseObject(XmlTag tag) {
        if (tag == null) {
            return null;
        }

        String xml = tag.getText();
        if (xml == null) {
            return null;
        }

        try {
            return PrismContext.get().parserFor(xml).parse();
        } catch (Exception ex) {
            return null;
        }
    }

    private List<XmlTag> getObjectFullPath(List<XmlTag> wholePath) {
        if (wholePath.isEmpty()) {
            return wholePath;
        }

        XmlTag tag = wholePath.get(0);
        if (QNameUtil.match(SchemaConstants.C_OBJECT, MidPointUtils.createQName(tag))) {
            return wholePath.subList(1, wholePath.size());
        }

        return wholePath;
    }

    private List<XmlTag> getParentTags(XmlTag tag) {
        List<XmlTag> tags = new ArrayList<>();

        XmlTag t = tag;
        while (t != null) {
            tags.add(t);

            t = t.getParentTag();
        }

        Collections.reverse(tags);

        return tags;
    }

    private void addSuggestionsFromEditorHint(Editor editor, String content, int cursorPosition, CompletionResultSet resultSet) {
        ItemDefinition<?> def = getObjectDefinitionFromHint(editor);
        addSuggestions(def, content, cursorPosition, resultSet);
    }

    private void addSuggestions(ItemDefinition<?> def, String content, int cursorPosition, CompletionResultSet resultSet) {
        if (def == null) return;

        AxiomQueryContentAssist axiomQueryContentAssist = new AxiomQueryContentAssistImpl(PrismContext.get());

        List<LookupElement> aliases = new ArrayList<>();
        List<LookupElement> suggestions = new ArrayList<>();
        cursorPosition = Math.max(0, cursorPosition);
        axiomQueryContentAssist.process(def, content, cursorPosition).autocomplete()
                .forEach(suggestion -> {
                    if (Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList().contains(suggestion.name())) {
                        aliases.add(build(suggestion.name(), suggestion.alias(), suggestion.priority()));
                    } else {
                        suggestions.add(build(suggestion.name(), suggestion.alias(), suggestion.priority()));
                    }
                });

        resultSet.addAllElements(suggestions);
        // This always shows filter Aliases
        CompletionResultSet customResultSet = resultSet.withPrefixMatcher("");
        customResultSet.addAllElements(aliases);
    }

    /**
     * returns for filter (SearchFilterType) tag if possible, otherwise returns tag which is parent of xml text.
     */
    private XmlTag findItemTag(XmlText xmlText) {
        XmlTag parentTag = xmlText.getParentTag();
        if (Objects.equals(MidPointUtils.createQName(parentTag), SearchFilterType.F_TEXT)) {
            XmlTag qTextParent = parentTag.getParentTag();
            QName qTextParentType = PsiUtils.getTagXsdType(qTextParent);
            if (SearchFilterType.COMPLEX_TYPE.equals(qTextParentType)) {
                return qTextParent.getParentTag();
            }
        }

        return parentTag;
    }

    private LookupElement build(String key, String alias, int priority) {
        if (alias == null) {
            alias = key;
        }

        LookupElementBuilder builder = LookupElementBuilder.create(key)
                .withTypeText(alias)
                .withLookupStrings(Arrays.asList(key, key.toLowerCase(), alias, alias.toLowerCase()))
                .withBoldness(true)
                .withCaseSensitivity(true);

        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        // paring tokens
        HashMap<String, String> pairTokens = new HashMap<>();
        Arrays.stream(Filter.Token.Pair.values()).forEach(pair -> {
            pairTokens.put(pair.getOpen(), pair.getClose());
        });

        if (pairTokens.containsKey(key)) {
            return PrioritizedLookupElement.withPriority(new LookupElementDecorator<>(element) {
                @Override
                public void handleInsert(@NotNull InsertionContext context) {
                    Editor editor = context.getEditor();
                    int offset = editor.getCaretModel().getOffset();
                    editor.getDocument().insertString(offset, pairTokens.get(key));
                    editor.getCaretModel().moveToOffset(offset);
                    PsiDocumentManager.getInstance(context.getProject()).commitDocument(editor.getDocument());
                }
            }, priority);
        } else {
            return PrioritizedLookupElement.withPriority(element, priority);
        }
    }

    private PsiElement findItemNameElement(PsiElement element) {
        if (element == null) return null;

        while (!element.getNode().getElementType().toString().equals(AxiomQueryParser.ruleNames[AxiomQueryParser.RULE_itemName])) {
            if (element.getNode().getElementType().toString().equals(AxiomQueryParser.ruleNames[AxiomQueryParser.RULE_root])) {
                break;
            }
            element = element.getParent();
        }

        return element;
    }
}
