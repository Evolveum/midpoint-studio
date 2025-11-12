/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.dialog;

/**
 * Created by Dominik.
 */
public interface DialogWindowActionHandler {

    /**
     * Whether the OK button should be visible.
     */
    default boolean isOkButtonVisible() {
        return true;
    }

    /**
     * Whether the Apply button should be visible.
     */
    default boolean isApplyButtonVisible() {
        return false;
    }

    /**
     * Whether the Cancel button should be visible.
     */
    default boolean isCancelButtonVisible() {
        return true;
    }

    /**
     * Whether the OK button is enabled.
     */
    default boolean isOkButtonEnabled() {
        return true;
    }

    /**
     * Whether the Apply button is enabled.
     */
    default boolean isApplyButtonEnabled() {
        return true;
    }

    /**
     * Whether the Cancel button is enabled.
     */
    default boolean isCancelButtonEnabled() {
        return true;
    }

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
