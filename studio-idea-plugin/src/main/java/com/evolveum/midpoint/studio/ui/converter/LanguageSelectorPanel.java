package com.evolveum.midpoint.studio.ui.converter;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;

/**
 * Created by Dominik.
 */
public class LanguageSelectorPanel extends JPanel {

    private final ComboBox<String> comboBox;

    public LanguageSelectorPanel(String title) {
        comboBox = new ComboBox<>(new String[]{
                XMLLanguage.INSTANCE.getID(),
                JsonLanguage.INSTANCE.getID(),
                YAMLLanguage.INSTANCE.getID()
        });
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