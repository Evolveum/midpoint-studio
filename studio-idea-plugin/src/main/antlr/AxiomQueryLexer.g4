lexer grammar AxiomQueryLexer;

NULL: 'null';
TRUE: 'true';
FALSE: 'false';

SEMICOLON : ';';
LEFT_BRACE : '{';
RIGHT_BRACE : '}';
COLON : ':';
PLUS : '+';
//LINE_COMMENT :  [ \n\r\t]* ('//' (~[\r\n]*)) [ \n\r\t]* -> channel(HIDDEN);
LINE_COMMENT : '//' .*? ('\n'|EOF)	-> channel(HIDDEN) ;
SEP: [ \n\r\t]+ -> channel(HIDDEN);

AND_KEYWORD: 'and'|'AND';
OR_KEYWORD: 'or'|'OR';
NOT_KEYWORD: 'not'|'NOT';
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_\-]*;

fragment SQOUTE : '\'';
fragment DQOUTE : '"';
fragment BACKTICK : '`';

fragment ESC : '\\';
//fragment ESC: '\\' ( ["\\/bfnrt] | UNICODE);

STRING_SINGLEQUOTE: SQOUTE ((ESC SQOUTE) | ~[\n'])* SQOUTE;
STRING_DOUBLEQUOTE: DQOUTE ((ESC DQOUTE) | ~[\n"])* DQOUTE;

STRING_MULTILINE: '"""' ('\r')? '\n' .*?  '"""';

//STRING_MULTILINE_START: '"""' ('\r')? '\n';

STRING_BACKTICK: BACKTICK ((ESC SQOUTE) | ~[\n'])* BACKTICK;


STRING_BACKTICK_TRIQOUTE: '```' ('\r')? '\n' .*? '```';
//STRING_BACKTICK_START: '```' ('\r')? '\n';

fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
fragment NONZERO_DIGIT: [1-9];
fragment DIGIT: [0-9];
fragment FRACTIONAL_PART: '.' DIGIT+;
fragment EXPONENTIAL_PART: EXPONENT_INDICATOR SIGN? DIGIT+;
fragment EXPONENT_INDICATOR: [eE];
fragment SIGN: [+-];
fragment NEGATIVE_SIGN: '-';

FLOAT: INT FRACTIONAL_PART
    | INT EXPONENTIAL_PART
    | INT FRACTIONAL_PART EXPONENTIAL_PART
    ;

INT: NEGATIVE_SIGN? '0'
    | NEGATIVE_SIGN? NONZERO_DIGIT DIGIT*
    ;

AT_SIGN: '@';
DOLLAR: '$';
SLASH: '/';
PARENT: '..';
SHARP: '#';
SQUARE_BRACKET_LEFT: '[';
SQUARE_BRACKET_RIGHT: ']';
ROUND_BRACKET_LEFT: '(';
ROUND_BRACKET_RIGHT: ')';
DOT: '.';
LT: '<';
LT_EQ: '<=';
GT: '>';
GT_EQ: '>=';
EQ: '=';
NOT_EQ: '!=';
COMMA: ',';
QUESTION_MARK: '?';
ERRCHAR:	.	-> channel(HIDDEN);
