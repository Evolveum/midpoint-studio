parser grammar StudioPropertiesParser;

@header {
package com.evolveum.midpoint.studio.lang.properties.antlr;
}

options { tokenVocab=StudioPropertiesLexer; }

pathItem
    : PATH_PARENT   #parentName
    | PATH_SELF     #selfName
    | IDENTIFIER    #fileName
    ;

path
    : (SLASH)? pathItem (SLASH pathItem)*   #filePath
    ;

property
    : AT_SIGN SEPARATOR* path   #fileContent
    | IDENTIFIER                #propertyName
    ;

root
    : SEPARATOR* DOLLAR_SIGN LEFT_BRACKET SEPARATOR* property SEPARATOR* RIGHT_BRACKET SEPARATOR* EOF
    | EOF
    ;
