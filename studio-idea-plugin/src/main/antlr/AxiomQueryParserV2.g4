parser grammar AxiomQueryParserV2;

@header {
package com.evolveum.midpoint.studio.lang.axiomquery.antlr;
}

options { tokenVocab=AxiomQueryLexerV2; }

root
    : SEP* filter SEP*
    ; // Needed for trailing spaces if multiline
filter
    : left=filter (SEP+ AND_KEYWORD SEP+ right=filter)  #andFilter
    | left=filter (SEP+ OR_KEYWORD SEP+ right=filter)   #orFilter
    | itemFilter                                        #genFilter
    | subfilterSpec                                     #subFilter;
subfilterSpec
        : ROUND_BRACKET_LEFT SEP* filter SEP* ROUND_BRACKET_RIGHT
        ;
itemFilter
    : (path SEP* usedAlias=filterNameAlias (matchingRule)? SEP* (subfilterOrValue))
    | (path (SEP+ negation)? SEP+ usedFilter=filterName (matchingRule)? (SEP+ (subfilterOrValue))?)
    ;
matchingRule
    : SQUARE_BRACKET_LEFT prefixedName SQUARE_BRACKET_RIGHT
    ;
nullLiteral
    : NULL
    ;
booleanLiteral
    : TRUE
    | FALSE
    ;
intLiteral
    : INT
    ;
floatLiteral
    : FLOAT
    ;
stringLiteral
    : STRING_SINGLEQUOTE    #singleQuoteString
    | STRING_DOUBLEQUOTE    #doubleQuoteString
    | STRING_MULTILINE      # multilineString
    ;
literalValue
    : value=(TRUE | FALSE)  #booleanValue
    | value=INT             #intValue
    | value=FLOAT           #floatValue
    | stringLiteral         #stringValue
    | NULL                  #nullValue
    ;
itemName
    : prefixedName          #dataName
    | AT_SIGN prefixedName  #infraName
    ;
prefixedName
    : (prefix=IDENTIFIER COLON)? localName=IDENTIFIER
    | (prefix=IDENTIFIER)? COLON localName=(AND_KEYWORD | NOT_KEYWORD | OR_KEYWORD)
    ;
argument
    : prefixedName | literalValue
    ;
// Axiom Path (different from Prism Item Path)
variable
    : DOLLAR itemName
    ;
parent
    : PARENT
    ;
// Path could start with ../ or context variable ($var) or item name
firstComponent
    : (parent ( SLASH parent )*) | variable | pathComponent
    ;
axiomPath
    : firstComponent ( SLASH pathComponent)*
    ;
pathComponent
    : itemName (pathValue)?
    ;
pathValue
    : SQUARE_BRACKET_LEFT argument SQUARE_BRACKET_RIGHT
    ;
itemPathComponent: SHARP    #IdentifierComponent
    | AT_SIGN               #DereferenceComponent
    | prefixedName          #ItemComponent
    ;
path
    : DOT                                                   #SelfPath
    | parent ( SLASH parent)* ( SLASH itemPathComponent)*   #ParentPath
    | itemPathComponent ( SLASH itemPathComponent)*         #DescendantPath
    | axiomPath                                             #PathAxiomPath;
filterNameAlias
    : EQ
    | LT
    | GT
    | LT_EQ
    | GT_EQ
    | NOT_EQ
    ;
filterName
    : prefixedName
    | filterNameAlias
    ;

// Currently value could be string or path
singleValue: literalValue | path;
valueSet: ROUND_BRACKET_LEFT SEP* values+=singleValue SEP* (COMMA SEP* values+=singleValue SEP*)* ROUND_BRACKET_RIGHT;

negation: NOT_KEYWORD;
// Filter could be Value filter or Logic Filter

subfilterOrValue : subfilterSpec | expression | singleValue | valueSet;

expression : script | constant;
script: (language=IDENTIFIER)? (scriptSingleline | scriptMultiline);
scriptSingleline : STRING_BACKTICK;
scriptMultiline : STRING_BACKTICK_TRIQOUTE;
constant: AT_SIGN name=IDENTIFIER;
