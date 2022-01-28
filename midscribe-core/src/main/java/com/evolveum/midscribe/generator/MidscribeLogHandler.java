package com.evolveum.midscribe.generator;

import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidscribeLogHandler implements LogHandler {

    private LogListener listener;

    public MidscribeLogHandler(LogListener listener) {
        Validate.notNull(listener, "Listener must not be null");

        this.listener = listener;
    }

    @Override
    public void log(LogRecord logRecord) {
        Cursor cursor = logRecord.getCursor();
        String sourceFileName = logRecord.getSourceFileName();
        String sourceMethodName = logRecord.getSourceMethodName();

        LogListener.Level level = LogListener.Level.getLevelBySeverity(logRecord.getSeverity());

        LogListener.MessageDetails details = new LogListener.MessageDetails(
                cursor.getLineNumber(),
                cursor.getPath(),
                cursor.getDir(),
                cursor.getFile(),
                sourceFileName,
                sourceMethodName
        );

        listener.log(level, logRecord.getMessage(), details);
    }
}
