package com.evolveum.midpoint.studio.ui.smart.suggestion;

/**
 * Created by Dominik.
 */
public interface DialogWindowActionHandler {

    /**
     * Title (text) for the OK button.
     */
    default String getOkButtonTitle() {
        return "OK";
    }

    default void onOk() {}

    /**
     * Title (text) for the Apply button.
     */
    default String getApplyButtonTitle() {
        return "Apply";
    }

    default void onApply() {}

    /**
     * Title (text) for the Cancel button.
     */
    default String getCancelButtonTitle() {
        return "Cancel";
    }

    default void onCancel() {}
}
