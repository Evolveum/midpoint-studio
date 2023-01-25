lexer grammar StudioPropertiesLexer;

@header {
package com.evolveum.midpoint.studio.lang.properties.antlr;
}

LEFT_BRACKET
    : '('
    ;
RIGHT_BRACKET
    : ')'
    ;
DOLLAR_SIGN
    : '$'
    ;
AT_SIGN
    : '@'
    ;
PATH_PARENT
    : '..'
    ;
PATH_SELF
    : '.'
    ;
SLASH
    : '/'
    ;
SEPARATOR: [ \n\r\t]+;
IDENTIFIER
    : [a-zA-Z0-9\-_.]+
    ;
