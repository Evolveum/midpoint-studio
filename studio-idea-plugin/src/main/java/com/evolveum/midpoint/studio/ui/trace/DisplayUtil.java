package com.evolveum.midpoint.studio.ui.trace;

import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

public class DisplayUtil {

    @NotNull
    public static String makeDisabled(Object value) {
        if (value != null) {
            return "<html><font color=\"" + Colors.DISABLED_COLOR + "\">" + StringEscapeUtils.escapeHtml(String.valueOf(value))
                    + "</font></html>";
        } else {
            return "";
        }
    }

    public static boolean isHtml(Object value) {
        return value instanceof String && ((String) value).startsWith("<html>");
    }

    public static String disableIfZero(int value) {
        if (value != 0) {
            return String.valueOf(value);
        } else {
            return makeDisabled(value);
        }
    }

    // brutal hack
    public static String disableIfZero(String value) {
        if (value == null) {
            return "";
        } else if (value.equals("0") || value.equals("0.0") || value.equals("0,0")) {
            return makeDisabled(value);
        } else {
            return value;
        }
    }
}
