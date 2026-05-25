package com.evolveum.midpoint.studio.lang.mel.impl;

import org.antlr.v4.runtime.Token;

public record ValidationMessage(Token token, ValidationSeverity severity, String message) {
    
}

