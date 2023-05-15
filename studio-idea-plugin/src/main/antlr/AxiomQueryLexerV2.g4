lexer grammar AxiomQueryLexerV2;

@header {
package com.evolveum.midpoint.studio.lang.axiomquery.antlr;
}

fragment UNICODE
    : 'u' HEX HEX HEX HEX
    ;
fragment HEX
    : [0-9a-fA-F]
    ;
fragment NONZERO_DIGIT
    : [1-9]
    ;
fragment DIGIT
    : [0-9]
    ;
fragment FRACTIONAL_PART
    : '.' DIGIT+
    ;
fragment EXPONENTIAL_PART
    : EXPONENT_INDICATOR SIGN? DIGIT+
    ;
fragment EXPONENT_INDICATOR
    : [eE]
    ;
fragment SIGN
    : [+-]
    ;
fragment NEGATIVE_SIGN
    : '-'
    ;
fragment SQOUTE
    : '\''
    ;
fragment DQOUTE
    : '"'
    ;
fragment BACKTICK
    : '`'
    ;
fragment ESC
    : '\\'
    ;

SEMICOLON
    : ';'
    ;
LEFT_BRACE
    : '{'
    ;
RIGHT_BRACE
    : '}'
    ;
SQUARE_BRACKET_LEFT
    : '['
    ;
SQUARE_BRACKET_RIGHT
    : ']'
    ;
ROUND_BRACKET_LEFT
    : '('
    ;
ROUND_BRACKET_RIGHT
    : ')'
    ;
COLON
    : ':'
    ;
COMMA
    : ','
    ;
PLUS
    : '+';
LINE_COMMENT
    :  [ \n\r\t]* ('//' (~[\r\n]*)) [ \n\r\t]* -> skip
    ;
SEP
    : [ \n\r\t]+
    ;
AND_KEYWORD
    : 'and'|'AND'
    ;
OR_KEYWORD
    : 'or'|'OR'
    ;
NOT_KEYWORD
    : 'not'|'NOT'
    ;
IDENTIFIER
    : [a-zA-Z_][a-zA-Z0-9_\-]*
    ;
STRING_MULTILINE
    : '"""' ('\r')? '\n' .*?  '"""'
    ;
FLOAT: INT FRACTIONAL_PART
    | INT EXPONENTIAL_PART
    | INT FRACTIONAL_PART EXPONENTIAL_PART
    ;
INT
    : NEGATIVE_SIGN? '0'
    | NEGATIVE_SIGN? NONZERO_DIGIT DIGIT*
    ;
STRING_SINGLEQUOTE
    : SQOUTE ((ESC SQOUTE) | ~[\n'])* SQOUTE
    ;
STRING_DOUBLEQUOTE
    : DQOUTE ((ESC DQOUTE) | ~[\n"])* DQOUTE
    ;
STRING_BACKTICK
    : BACKTICK ((ESC SQOUTE) | ~[\n'])* BACKTICK
    ;
STRING_BACKTICK_TRIQOUTE
    : '```' ('\r')? '\n' .*? '```'
    ;
NULL
    : 'null'
    ;
TRUE
    : 'true'
    ;
FALSE
    : 'false'
    ;
AT_SIGN
    : '@'
    ;
DOLLAR
    : '$'
    ;
SLASH
    : '/'
    ;
PARENT
    : '..'
    ;
SHARP
    : '#'
    ;
DOT
    : '.'
    ;
LT
    : '<'
    ;
LT_EQ
    : '<='
    ;
GT
    : '>'
    ;
GT_EQ
    : '>='
    ;
EQ
    : '='
    ;
NOT_EQ
    : '!='
    ;