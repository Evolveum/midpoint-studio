package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import java.awt.*;

public class DiscoverDocumentation extends JBPanel<DiscoverDocumentation> {

    private final ConnectorGeneratorDataModel dataModel;

    private JPanel mainPanel;
    private JList<?> list1;
    private JTextPane subtext;
    private JLabel text;
    private JTextPane aiFoundDescription;
    private JPanel header;
    private JPanel aiAlert;
    private JLabel aiFoundTitle;
    private JPanel content;
    private JScrollPane listDocs;
    private JPanel itemDoc;
    private JCheckBox itemDocCheckbox;
    private JLabel itemDocTitle;
    private JTextPane itemDocDescription;
    private JLabel itemDocLink;
    private JPanel actionDocsPanel;
    private JButton uploadFileButton;
    private JButton addUrlButton;
    private JButton createNewButton;
    private JButton viewButton;
    private JButton deleteButton;

    public DiscoverDocumentation(ConnectorGeneratorDataModel dataModel) {
        this.dataModel = dataModel;
        add(mainPanel, BorderLayout.CENTER);
        initComponents();
    }

    private void initComponents() {

    }

    public JLabel getText() {
        return text;
    }

    public JTextPane getSubtext() {
        return subtext;
    }

    public JList getList1() {
        return list1;
    }
}
