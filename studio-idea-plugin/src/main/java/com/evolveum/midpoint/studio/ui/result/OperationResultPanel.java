package com.evolveum.midpoint.studio.ui.result;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.studio.ui.UiConstants;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private JLabel parametersLabel;
    private JLabel contextLabel;
    private JLabel messageLabel;

    public OperationResultPanel(OperationResult result) {
        super(new BorderLayout());

        add(root, BorderLayout.CENTER);

//        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        root.setBorder(BorderFactory.createLineBorder(getStatusColor(result.getStatus())));

        initLayout(result);
    }

    private Color getStatusColor(OperationResultStatus status) {
        switch (status) {
            case FATAL_ERROR:
            case PARTIAL_ERROR:
                return UiConstants.RED;
            case IN_PROGRESS:
            case NOT_APPLICABLE:
                return UiConstants.BLUE;
            case SUCCESS:
                return UiConstants.GREEN;
            case UNKNOWN:
            case WARNING:
            case HANDLED_ERROR:
            default:
                return UiConstants.ORANGE;
        }
    }

    private void initLayout(OperationResult result) {
        operation.setBackground(getStatusColor(result.getStatus()));
        operation.setText(result.getOperation());
        operation.setBorder(new EmptyBorder(5, 5, 5, 5));
        operation2.setText(result.getOperation());

        if (!StringUtils.isEmpty(result.getMessage())) {
            message.setText(result.getMessage());
        } else {
            messageLabel.setVisible(false);
            message.setVisible(false);
        }

        initKeyValuePanels(parametersLabel, parameters, result.getParams());
        initKeyValuePanels(contextLabel, context, result.getContext());

        if (result.getCount() > 1) {
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

    private void initKeyValuePanels(JLabel label, JPanel parent, Map<String, Collection<String>> map) {
        if (map == null || map.isEmpty()) {
            label.setVisible(false);
            parent.setVisible(false);
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
