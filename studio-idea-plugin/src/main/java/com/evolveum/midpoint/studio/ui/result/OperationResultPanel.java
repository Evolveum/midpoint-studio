package com.evolveum.midpoint.studio.ui.result;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.ui.UiConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationResultPanel extends JPanel {

    private JPanel root;

    private JLabel operation;
    private JLabel operation2;
    private JLabel message;
    private JPanel parameters;
    private JPanel context;
    private JLabel count;
    private JLabel countLabel;
    private JPanel subresults;

    public OperationResultPanel(OperationResult result) {
        super(new BorderLayout());

        add(root, BorderLayout.CENTER);

        initLayout(result);
    }

    private void initLayout(OperationResult result) {
        switch (result.getStatus()) {
            case FATAL_ERROR:
            case PARTIAL_ERROR:
                operation.setBackground(UiConstants.RED);
                break;
            case IN_PROGRESS:
            case NOT_APPLICABLE:
                operation.setBackground(UiConstants.BLUE);
                break;
            case SUCCESS:
                operation.setBackground(UiConstants.GREEN);
                break;
            case UNKNOWN:
            case WARNING:
            case HANDLED_ERROR:
                operation.setBackground(UiConstants.ORANGE);
                break;
        }
        operation.setText(result.getOperation());
        operation2.setText(result.getOperation());

        message.setText(result.getMessage());

        initKeyValuePanels(parameters, result.getParams());
        initKeyValuePanels(context, result.getContext());

        if (result.getCount() != 0) {
            count.setText(Integer.toString(result.getCount()));
        } else {
            countLabel.setVisible(false);
            count.setVisible(false);
        }

        if (result.getSubresults() != null && !result.getSubresults().isEmpty()) {
            for (OperationResult res : result.getSubresults()) {
                subresults.add(new OperationResultPanel(res));
            }
        } else {
            subresults.setVisible(false);
        }
    }

    private void initKeyValuePanels(JPanel parent, Map<String, Collection<String>> map) {
        if (map == null || parent == null) {
            return;
        }

        List<String> names = new ArrayList<>();
        names.addAll(map.keySet());
        Collections.sort(names);

        for (String name : names) {
            parent.add(new KeyValuePanel(name, map.get(name).toString()));
        }
    }

    private void createUIComponents() {
        parameters = new JPanel();
        parameters.setLayout(new BoxLayout(parameters, BoxLayout.Y_AXIS));

        context = new JPanel();
        context.setLayout(new BoxLayout(context, BoxLayout.Y_AXIS));

        subresults = new JPanel();
        subresults.setLayout(new BoxLayout(subresults, BoxLayout.Y_AXIS));
    }
}
