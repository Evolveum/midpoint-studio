package com.evolveum.midpoint.client.query;

import com.evolveum.midpoint.client.query.parser.QueryGrammarLexer;
import com.evolveum.midpoint.client.query.parser.QueryGrammarParser;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.QueryConverter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RuleTransfomer {

    private static final Logger LOG = LoggerFactory.getLogger(RuleTransfomer.class);

    public static QueryGrammarParser buildParser(String rule) {
        CharStream input = CharStreams.fromString(rule);

        QueryGrammarLexer lexer = new QueryGrammarLexer(new CaseChangingCharStream(input, true));
        TokenStream tokens = new CommonTokenStream(lexer);

        return new QueryGrammarParser(tokens);
    }

    public static SearchFilterType transformRule(String type, String rule, PrismContext prismContext)
            throws SchemaException {

        QueryGrammarParser parser = buildParser(rule);

        ObjectFilterBuilder filterBuilder = new ObjectFilterBuilder(type, prismContext);
        parser.addParseListener(filterBuilder);

        QueryGrammarParser.Rule_setContext ruleSet = parser.rule_set();
        LOG.debug("Rule " + ruleSet);

        ObjectFilter filter = filterBuilder.getObjectFilter();
        LOG.debug("Filter dump\n" + filter.debugDump());

        QueryConverter converter = prismContext.getQueryConverter();

        return converter.createSearchFilterType(filter);
    }
}
