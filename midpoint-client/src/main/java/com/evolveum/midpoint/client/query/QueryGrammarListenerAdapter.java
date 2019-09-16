package com.evolveum.midpoint.client.query;

import com.evolveum.midpoint.client.query.parser.QueryGrammarListener;
import com.evolveum.midpoint.client.query.parser.QueryGrammarParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Viliam Repan (lazyman).
 */
public class QueryGrammarListenerAdapter implements QueryGrammarListener {

    private static final Logger LOG = LoggerFactory.getLogger(QueryGrammarListenerAdapter.class);

    @Override
    public void enterRule_set(QueryGrammarParser.Rule_setContext ctx) {
        logMethod();
    }

    @Override
    public void exitRule_set(QueryGrammarParser.Rule_setContext ctx) {
        logMethod();
    }

    @Override
    public void enterLogicalEntity(QueryGrammarParser.LogicalEntityContext ctx) {
        logMethod();
    }

    @Override
    public void exitLogicalEntity(QueryGrammarParser.LogicalEntityContext ctx) {
        logMethod();
    }

    @Override
    public void enterComparisonExpression(QueryGrammarParser.ComparisonExpressionContext ctx) {
        logMethod();
    }

    @Override
    public void exitComparisonExpression(QueryGrammarParser.ComparisonExpressionContext ctx) {
        logMethod();
    }

    @Override
    public void enterLogicalExpressionInParen(QueryGrammarParser.LogicalExpressionInParenContext ctx) {
        logMethod();
    }

    @Override
    public void exitLogicalExpressionInParen(QueryGrammarParser.LogicalExpressionInParenContext ctx) {
        logMethod();
    }

    @Override
    public void enterLogicalExpressionAnd(QueryGrammarParser.LogicalExpressionAndContext ctx) {
        logMethod();
    }

    @Override
    public void exitLogicalExpressionAnd(QueryGrammarParser.LogicalExpressionAndContext ctx) {
        logMethod();
    }

    @Override
    public void enterLogicalExpressionNot(QueryGrammarParser.LogicalExpressionNotContext ctx) {
        logMethod();
    }

    @Override
    public void exitLogicalExpressionNot(QueryGrammarParser.LogicalExpressionNotContext ctx) {
        logMethod();
    }

    @Override
    public void enterLogicalExpressionOr(QueryGrammarParser.LogicalExpressionOrContext ctx) {
        logMethod();
    }

    @Override
    public void exitLogicalExpressionOr(QueryGrammarParser.LogicalExpressionOrContext ctx) {
        logMethod();
    }

    @Override
    public void enterComparisonExpressionWithOperator(QueryGrammarParser.ComparisonExpressionWithOperatorContext ctx) {
        logMethod();
    }

    @Override
    public void exitComparisonExpressionWithOperator(QueryGrammarParser.ComparisonExpressionWithOperatorContext ctx) {
        logMethod();
    }

    @Override
    public void enterComparisonIsNull(QueryGrammarParser.ComparisonIsNullContext ctx) {
        logMethod();
    }

    @Override
    public void exitComparisonIsNull(QueryGrammarParser.ComparisonIsNullContext ctx) {
        logMethod();
    }

    @Override
    public void enterComparisonIsNotNull(QueryGrammarParser.ComparisonIsNotNullContext ctx) {
        logMethod();
    }

    @Override
    public void exitComparisonIsNotNull(QueryGrammarParser.ComparisonIsNotNullContext ctx) {
        logMethod();
    }

    @Override
    public void enterInExpressionOperator(QueryGrammarParser.InExpressionOperatorContext ctx) {
        logMethod();
    }

    @Override
    public void exitInExpressionOperator(QueryGrammarParser.InExpressionOperatorContext ctx) {
        logMethod();
    }

    @Override
    public void enterNotInExpressionOperator(QueryGrammarParser.NotInExpressionOperatorContext ctx) {
        logMethod();
    }

    @Override
    public void exitNotInExpressionOperator(QueryGrammarParser.NotInExpressionOperatorContext ctx) {
        logMethod();
    }

    @Override
    public void enterComparisonExpressionParens(QueryGrammarParser.ComparisonExpressionParensContext ctx) {
        logMethod();
    }

    @Override
    public void exitComparisonExpressionParens(QueryGrammarParser.ComparisonExpressionParensContext ctx) {
        logMethod();
    }

    @Override
    public void enterComparisonWithFunctionCall(QueryGrammarParser.ComparisonWithFunctionCallContext ctx) {
        logMethod();
    }

    @Override
    public void exitComparisonWithFunctionCall(QueryGrammarParser.ComparisonWithFunctionCallContext ctx) {
        logMethod();
    }

    @Override
    public void enterIn_expr(QueryGrammarParser.In_exprContext ctx) {
        logMethod();
    }

    @Override
    public void exitIn_expr(QueryGrammarParser.In_exprContext ctx) {
        logMethod();
    }

    @Override
    public void enterComp_operator(QueryGrammarParser.Comp_operatorContext ctx) {
        logMethod();
    }

    @Override
    public void exitComp_operator(QueryGrammarParser.Comp_operatorContext ctx) {
        logMethod();
    }

    @Override
    public void enterValue_entity(QueryGrammarParser.Value_entityContext ctx) {
        logMethod();
    }

    @Override
    public void exitValue_entity(QueryGrammarParser.Value_entityContext ctx) {
        logMethod();
    }

    @Override
    public void enterLogicalConst(QueryGrammarParser.LogicalConstContext ctx) {
        logMethod();
    }

    @Override
    public void exitLogicalConst(QueryGrammarParser.LogicalConstContext ctx) {
        logMethod();
    }

    @Override
    public void enterNumericConst(QueryGrammarParser.NumericConstContext ctx) {
        logMethod();
    }

    @Override
    public void exitNumericConst(QueryGrammarParser.NumericConstContext ctx) {
        logMethod();
    }

    @Override
    public void visitTerminal(TerminalNode node) {

    }

    @Override
    public void visitErrorNode(ErrorNode node) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {

    }

    private void logMethod() {
        String method = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOG.debug(method);
    }
}
