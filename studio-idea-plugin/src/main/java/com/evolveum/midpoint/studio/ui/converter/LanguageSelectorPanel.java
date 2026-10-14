package com.evolveum.midpoint.studio.ui.converter;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;

/**
 * Created by Dominik.
 */
public class LanguageSelectorPanel extends JPanel {

    private final ComboBox<String> comboBox;

    public LanguageSelectorPanel(String title, String[] languages) {
        comboBox = new ComboBox<>(languages);
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