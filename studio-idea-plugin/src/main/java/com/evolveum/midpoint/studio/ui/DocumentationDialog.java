package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.DocGeneratorOptions;
import com.evolveum.midpoint.studio.util.EnumComboBoxModel;
import com.evolveum.midscribe.generator.export.ExportFormat;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DocumentationDialog extends DialogWrapper {

    private JPanel root;
    private TextFieldWithBrowseButton sourceDirectory;
    private JTextArea include;
    private JTextArea exclude;
    private JComboBox exportFormat;
    private TextFieldWithBrowseButton output;

    public DocumentationDialog(Project project, DocGeneratorOptions options) {
        super(false);

        setTitle("Documentation settings");
        setSize(400,200);

        initLayout(project);

        populateFields(options);

        init();
    }

    private void initLayout(Project project) {
        sourceDirectory.addBrowseFolderListener("Select source folder", "Folder where MidPoint object xml files are stored", project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor());

        output.addBrowseFolderListener("Select output file", null, project,
                FileChooserDescriptorFactory.createSingleLocalFileDescriptor());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return sourceDirectory;
    }

    private void populateFields(DocGeneratorOptions opts) {
        if (opts.getSourceDirectory() != null) {
            sourceDirectory.setText(opts.getSourceDirectory().getPath());
        }

        StringBuilder isb = new StringBuilder();
        opts.getInclude().forEach(i -> isb.append(i).append('\n'));
        include.setText(isb.toString());

        StringBuilder esb = new StringBuilder();
        opts.getExclude().forEach(i -> esb.append(i).append('\n'));
        exclude.setText(esb.toString());

        exportFormat.getModel().setSelectedItem(opts.getExportFormat());

        if (opts.getExportOutput() != null) {
            output.setText(opts.getExportOutput().getPath());
        }
    }

    private void createUIComponents() {
        exportFormat = new ComboBox();
        EnumComboBoxModel gm = new EnumComboBoxModel(ExportFormat.class, true);
        exportFormat.setModel(gm);
        exportFormat.getModel().setSelectedItem(ExportFormat.HTML);
//        exportFormat.setRenderer(new LocalizedRenderer());    // todo improve
    }

    public DocGeneratorOptions getOptions() {
        DocGeneratorOptions opts = new DocGeneratorOptions();

        if (sourceDirectory.getText() != null) {
            opts.setSourceDirectory(new File(sourceDirectory.getText()));
        }

        List<String> lines = parseRows(include);
        opts.setInclude(lines);

        lines = parseRows(exclude);
        opts.setExclude(lines);

        opts.setExportFormat((ExportFormat) exportFormat.getSelectedItem());

        if (output.getText() != null) {
            opts.setExportOutput(new File(output.getText()));
        }

        return opts;
    }

    private List<String> parseRows(JTextArea area) {
        if (StringUtils.isEmpty(area.getText())) {
            return new ArrayList<>();
        }

        String[] s = area.getText().split("\n");
        return Arrays.asList(s).stream().filter(i -> StringUtils.isNotEmpty(i)).collect(Collectors.toList());
    }
}
