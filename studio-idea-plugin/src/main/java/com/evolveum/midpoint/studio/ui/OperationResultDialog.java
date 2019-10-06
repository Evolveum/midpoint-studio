package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.ui.result.OperationResultPanel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultDialog extends DialogWrapper {

    private JBScrollPane panel;

    public OperationResultDialog(@NotNull OperationResult result) {
        super(false);

        setTitle(result.getOperation());
        setSize(200, 100);

        this.panel = new JBScrollPane(new OperationResultPanel(result));

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}
