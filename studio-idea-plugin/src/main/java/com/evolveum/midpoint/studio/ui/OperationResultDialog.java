package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultDialog extends DialogWrapper {

    private OperationResultPanel panel;

    public OperationResultDialog(@NotNull OperationResult result) {
        super(false);

        setTitle(result.getOperation());

        this.panel = new OperationResultPanel(result);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}
