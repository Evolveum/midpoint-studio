package com.evolveum.midpoint.client.query;

import com.evolveum.midpoint.client.query.parser.QueryGrammarParser;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.impl.query.SubstringFilterImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectFilterBuilder extends QueryGrammarListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectFilterBuilder.class);

    private String type;

    private PrismContext prismContext;

    private QueryFactory queryFactory;

    private ObjectFilter filter;

    private Stack<Stack<ObjectFilter>> expressions = new Stack<>();

    public ObjectFilter getObjectFilter() {
        return filter;
    }

    public ObjectFilterBuilder(String type, PrismContext prismContext) {
        this.type = type;
        this.prismContext = prismContext;
        this.queryFactory = prismContext.queryFactory();
    }

    @Override
    public void enterRule_set(QueryGrammarParser.Rule_setContext ctx) {
        super.enterRule_set(ctx);

        filter = null;

        expressions.empty();
        expressions.push(new Stack<>());
    }

    @Override
    public void exitRule_set(QueryGrammarParser.Rule_setContext ctx) {
        if (expressions.isEmpty()) {
            filter = null;
            return;
        }

        Stack<ObjectFilter> stack = expressions.pop();
        filter = stack.isEmpty() ? null : stack.pop();
    }

    @Override
    public void exitLogicalExpressionAnd(QueryGrammarParser.LogicalExpressionAndContext ctx) {
        super.exitLogicalExpressionAnd(ctx);

        int arguments = ctx.logical_expr().size();
        AndFilter and = queryFactory.createAnd();

        exitLogicalExpression(and, arguments);
    }

    @Override
    public void exitLogicalExpressionOr(QueryGrammarParser.LogicalExpressionOrContext ctx) {
        super.exitLogicalExpressionOr(ctx);

        int arguments = ctx.logical_expr().size();
        OrFilter or = queryFactory.createOr();

        exitLogicalExpression(or, arguments);
    }

    @Override
    public void exitLogicalExpressionNot(QueryGrammarParser.LogicalExpressionNotContext ctx) {
        super.exitLogicalExpressionNot(ctx);

        ObjectFilter child = expressions.peek().pop();
        NotFilter not = queryFactory.createNot(child);

        expressions.peek().push(not);
    }

    private void exitLogicalExpression(NaryLogicalFilter filter, int argumentsCount) {
        for (int i = 0; i < argumentsCount; i++) {
            filter.addCondition(expressions.peek().pop());
        }

        expressions.peek().push(filter);
    }

    @Override
    public void exitComparisonExpressionWithOperator(QueryGrammarParser.ComparisonExpressionWithOperatorContext ctx) {
        super.exitComparisonExpressionWithOperator(ctx);

        String columnName = ctx.IDENTIFIER().getText();

        ItemPath path = null; // todo mapper.getItemPath(type, columnName);
        PrismPropertyDefinition definition = findDefinition(path);

        String operator = ctx.comp_operator().getText().toLowerCase();

        QueryGrammarParser.Value_entityContext rawValue = ctx.value_entity();
        Object value = transformRawValue(rawValue, definition);

        ObjectFilter filter;
        switch (operator) {
            case "=":
                if (isOverrideWhereCondition(ctx)) {
                    filter = createOverrideEqualFilter(columnName, value);
                    break;
                }
                filter = createEqualFilter(columnName, value);
                break;
            case "<>":
            case "!=":
                filter = queryFactory.createNot(createEqualFilter(columnName, value));
                break;
            case "like":
                filter = createSubstringFilter(columnName, value);
                break;
            default:
                throw new RuntimeException("Unknown operator '" + operator + "'");
        }

        if (filter == null) {
            return;
        }

        expressions.peek().push(filter);
    }

    @Override
    public void exitInExpressionOperator(QueryGrammarParser.InExpressionOperatorContext ctx) {
        super.exitInExpressionOperator(ctx);

        OrFilter or = translateInExpression(ctx.IDENTIFIER().getText(), ctx.in_expr());
        if (or == null) {
            return;
        }

        expressions.peek().push(or);
    }

    @Override
    public void exitNotInExpressionOperator(QueryGrammarParser.NotInExpressionOperatorContext ctx) {
        super.exitNotInExpressionOperator(ctx);

        OrFilter or = translateInExpression(ctx.IDENTIFIER().getText(), ctx.in_expr());
        if (or == null) {
            return;
        }

        NotFilter nor = queryFactory.createNot(or);

        expressions.peek().push(nor);
    }

    private OrFilter translateInExpression(String columnName, QueryGrammarParser.In_exprContext inExprCxt) {
        ItemPath path = null; // todo mapper.getItemPath(type, columnName);
        PrismPropertyDefinition definition = findDefinition(path);

        List<QueryGrammarParser.Value_entityContext> rawValues = inExprCxt.getRuleContexts(QueryGrammarParser.Value_entityContext.class);

        List<Object> values = new ArrayList<>();
        rawValues.forEach(v -> values.add(transformRawValue(v, definition)));

        OrFilter or = queryFactory.createOr();
        for (Object value : values) {
            EqualFilter eq = createEqualFilter(columnName, value);
            or.addCondition(eq);
        }

        return or;
    }

    @Override
    public void exitComparisonIsNull(QueryGrammarParser.ComparisonIsNullContext ctx) {
        super.exitComparisonIsNull(ctx);

        EqualFilter eq = createEqualFilter(ctx.IDENTIFIER().getText());
        expressions.peek().push(eq);
    }

    @Override
    public void exitComparisonIsNotNull(QueryGrammarParser.ComparisonIsNotNullContext ctx) {
        super.exitComparisonIsNotNull(ctx);

        EqualFilter eq = createEqualFilter(ctx.IDENTIFIER().getText());
        NotFilter nq = queryFactory.createNot(eq);

        expressions.peek().push(nq);
    }

    /**
     * See function f_vip_or_not, this method will only work for comparison equal to f_vip_or_not(tfn_id)='Y'
     * (super stupid impl, since we can't handle function in MidPoint query language)
     *
     * @param ctx
     */
    @Override
    public void exitComparisonWithFunctionCall(QueryGrammarParser.ComparisonWithFunctionCallContext ctx) {
        super.exitComparisonWithFunctionCall(ctx);

        String function = ctx.IDENTIFIER(0).getText().toLowerCase();

        // todo handle function calls
    }

    private GreaterFilter createGreaterFilter(String columnName, boolean equals, Object value) {
        ItemPath path = null; // todo mapper.getItemPath(type, columnName);
        PrismPropertyDefinition def = findDefinition(path);

        GreaterFilter greater = queryFactory.createGreater(path, def, equals);
        greater.setValue(prismContext.itemFactory().createPropertyValue(value));

        return greater;
    }

    private LessFilter createLessFilter(String columnName, boolean equals, Object value) {
        ItemPath path = null; // todo mapper.getItemPath(type, columnName);
        PrismPropertyDefinition def = findDefinition(path);

        LessFilter less = queryFactory.createLess(path, def, equals);
        less.setValue(prismContext.itemFactory().createPropertyValue(value));

        return less;
    }

    private SubstringFilter createSubstringFilter(String columnName, Object value) {
        ItemPath path = null; // todo mapper.getItemPath(type, columnName);
        PrismPropertyDefinition definition = findDefinition(path);

        // todo handle anchors '%some_value%'
        boolean anchorStart = false;
        boolean anchorEnd = false;

        return SubstringFilterImpl.createSubstring(path, definition, prismContext, null, value, anchorStart, anchorEnd);
    }

    private boolean isOverrideWhereCondition(QueryGrammarParser.ComparisonExpressionWithOperatorContext ctx) {
        if (!(ctx.getParent() instanceof QueryGrammarParser.Logical_exprContext)) {
            return false;
        }

//        if (!(ctx.getParent().getParent() instanceof QueryGrammarParser.SelectQueryContext)) {
//            return false;
//        }
//
//        QueryGrammarParser.SelectQueryContext sqc = (QueryGrammarParser.SelectQueryContext) ctx.getParent().getParent();
//
//        String column = sqc.IDENTIFIER(0).getText();
//        if (!Constants.OVERRIDE_PER_ID.equalsIgnoreCase(column)) {
//            return false;
//        }
//
//        String table = sqc.table().getText();
//        if (!Constants.OVERRIDE_TABLE.equalsIgnoreCase(table)) {
//            return false;
//        }

        return true;
    }

    private EqualFilter createOverrideEqualFilter(String columnName, Object value) {
        if (value == null || !(value instanceof String)) {
            throw new IllegalStateException("Unknown value for " + columnName);
        }

        String sValue = (String) value;
        Boolean bValue = null; // todo Constants.BOOLEAN_YES.equalsIgnoreCase(sValue) ? true : false;

        return createEqualFilter(columnName, bValue);
    }

    private EqualFilter createEqualFilter(String columnName, Object... value) {
        ItemPath path = null; // todo mapper.getItemPath(type, columnName);
        PrismPropertyDefinition definition = findDefinition(path);

        return queryFactory.createEqual(path, definition, null, prismContext, value);
    }

    private PrismPropertyDefinition findDefinition(ItemPath path) {
        if (path == null) {
            return null;
        }

        SchemaRegistry registry = prismContext.getSchemaRegistry();

        Class<? extends ObjectType> clazz = null; // todo mapper.getType(type);
        PrismObjectDefinition objectDef = registry.findObjectDefinitionByCompileTimeClass(clazz);

        return objectDef.findItemDefinition(path);
    }

    private Object transformRawValue(QueryGrammarParser.Value_entityContext val, PrismPropertyDefinition definition) {
        return StringUtils.unwrap(val.getText(), "'");
    }
}
