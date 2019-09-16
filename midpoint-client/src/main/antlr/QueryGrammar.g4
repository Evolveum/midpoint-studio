grammar QueryGrammar;

@header {
    package com.evolveum.midpoint.client.query.parser;
}

/* Lexical rules */

SPACE   :  [ \t\r\n]+    -> channel(HIDDEN);

AND : 'AND';
OR  : 'OR';

IN  : 'IN';

TRUE  : 'TRUE' ;
FALSE : 'FALSE' ;

IS   : 'IS';
NOT  : 'NOT';
NULL : 'NULL';

GT : '>' ;
GE : '>=' ;
LT : '<' ;
LE : '<=' ;
EQ : '=' ;
NEQ: '<>';
NEQ2: '!=';

LIKE: 'LIKE';

LPAREN : '(' ;
RPAREN : ')' ;

STRING_LITERAL: DQUOTA_STRING | SQUOTA_STRING;

DECIMAL : '-'?[0-9]+('.'[0-9]+)? ;

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9/]* ;

/* Grammar rules */

rule_set : logical_expr EOF ;

logical_expr
 : logical_expr AND logical_expr    # LogicalExpressionAnd
 | logical_expr OR logical_expr     # LogicalExpressionOr
 | NOT LPAREN logical_expr RPAREN   # LogicalExpressionNot
 | comparison_expr                  # ComparisonExpression
 | LPAREN logical_expr RPAREN       # LogicalExpressionInParen
 | logical_entity                   # LogicalEntity
 ;

comparison_expr
 : IDENTIFIER comp_operator value_entity                                                # ComparisonExpressionWithOperator
 | IDENTIFIER IS NULL                                                                   # ComparisonIsNull
 | IDENTIFIER IS NOT NULL                                                               # ComparisonIsNotNull
 | IDENTIFIER IN LPAREN in_expr RPAREN                                                  # InExpressionOperator
 | IDENTIFIER NOT IN LPAREN in_expr RPAREN                                              # NotInExpressionOperator
 | LPAREN comparison_expr RPAREN                                                        # ComparisonExpressionParens
 | IDENTIFIER LPAREN IDENTIFIER ( ',' IDENTIFIER )* RPAREN comp_operator value_entity   # ComparisonWithFunctionCall
 ;

in_expr
 : value_entity ( ',' value_entity )*
 ;

comp_operator
 : EQ
 | NEQ
 | LIKE
 | GT
 | GE
 | LT
 | LE
 ;

value_entity
 : logical_entity
 | numeric_entity
 | STRING_LITERAL
 ;

logical_entity
 : (TRUE | FALSE) # LogicalConst
 ;

numeric_entity
 : DECIMAL              # NumericConst
 ;

fragment DQUOTA_STRING:              '"' ( '\\'. | '""' | ~('"'| '\\') )* '"';
fragment SQUOTA_STRING:              '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\'';