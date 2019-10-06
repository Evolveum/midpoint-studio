package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.schema.result.OperationResult;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultPanel extends JPanel {

    private JPanel root;

    private JLabel operation;
    private JLabel operation2;

    public OperationResultPanel(OperationResult result) {
        super(new BorderLayout());

        add(root, BorderLayout.CENTER);

        initLayout(result);
    }

    private void initLayout(OperationResult result) {
        operation.setText(result.getOperation());
        operation2.setText(result.getOperation());
    }
}
