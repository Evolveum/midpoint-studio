package com.evolveum.midpoint.studio.ui.converter;

import javax.swing.*;

/**
 * Created by Dominik.
 */
public class LanguageSelectorPanel extends JPanel {

    private final JComboBox<String> comboBox;

    public LanguageSelectorPanel(String title) {
        comboBox = new JComboBox<>(new String[]{"xml", "json", "yaml"});
        add(new JLabel(title));
        add(comboBox);
    }

    public void onLanguageChange(java.awt.event.ActionListener listener) {
        comboBox.addActionListener(listener);
    }

    public String getSelectedLanguage() {
        return (String) comboBox.getSelectedItem();
    }

    public void setSelectedLanguage(String lang) {
        comboBox.setSelectedItem(lang);
    }
}