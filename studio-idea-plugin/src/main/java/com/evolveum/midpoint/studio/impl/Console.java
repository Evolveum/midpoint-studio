package com.evolveum.midpoint.studio.impl;

import com.intellij.execution.ui.ConsoleViewContentType;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface Console {

    enum ContentType {

        DEBUG_OUTPUT(ConsoleViewContentType.LOG_DEBUG_OUTPUT),

        VERBOSE_OUTPUT(ConsoleViewContentType.LOG_VERBOSE_OUTPUT),

        INFO_OUTPUT(ConsoleViewContentType.LOG_INFO_OUTPUT),

        WARNING_OUTPUT(ConsoleViewContentType.LOG_WARNING_OUTPUT),

        ERROR_OUTPUT(ConsoleViewContentType.ERROR_OUTPUT);

        private final ConsoleViewContentType type;

        ContentType(ConsoleViewContentType type) {
            this.type = type;
        }

        public ConsoleViewContentType getType() {
            return type;
        }
    }

    void printToConsole(Environment env, Class clazz, String message);

    void printToConsole(Environment env, Class clazz, String message, Exception ex);

    void printToConsole(Environment env, Class clazz, String message, Exception ex, ContentType type);
}
