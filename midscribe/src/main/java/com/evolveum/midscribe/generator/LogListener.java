package com.evolveum.midscribe.generator;

import org.asciidoctor.log.Severity;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface LogListener {

    enum Level {

        INFO(Severity.INFO),

        WARN(Severity.WARN),

        ERROR(Severity.ERROR),

        FATAL(Severity.FATAL),

        DEBUG(Severity.DEBUG),

        UNKNOWN(Severity.UNKNOWN);

        private Severity severity;

        Level(Severity severity) {
            this.severity = severity;
        }

        Severity getSeverity() {
            return severity;
        }

        static Level getLevelBySeverity(Severity severity) {
            if (severity == null) {
                return UNKNOWN;
            }

            for (Level l : values()) {
                if (severity.equals(severity)) {
                    return l;
                }
            }

            return UNKNOWN;
        }
    }

    class MessageDetails {

        private int lineNumber;

        private String path;

        private String dir;

        private String file;

        private String sourceFileName;

        private String sourceMethodName;

        public MessageDetails(int lineNumber, String path, String dir, String file, String sourceFileName, String sourceMethodName) {
            this.lineNumber = lineNumber;
            this.path = path;
            this.dir = dir;
            this.file = file;
            this.sourceFileName = sourceFileName;
            this.sourceMethodName = sourceMethodName;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getPath() {
            return path;
        }

        public String getDir() {
            return dir;
        }

        public String getFile() {
            return file;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public String getSourceMethodName() {
            return sourceMethodName;
        }

        @Override
        public String toString() {
            return "Details{" +
                    "line=" + lineNumber +
                    ", path='" + path + '\'' +
                    ", dir='" + dir + '\'' +
                    ", file='" + file + '\'' +
                    ", sourceFileName='" + sourceFileName + '\'' +
                    ", sourceMethodName='" + sourceMethodName + '\'' +
                    '}';
        }
    }

    void log(Level level, String message, MessageDetails details);

}
