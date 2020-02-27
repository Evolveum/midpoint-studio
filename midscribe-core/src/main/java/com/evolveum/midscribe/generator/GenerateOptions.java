package com.evolveum.midscribe.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GenerateOptions {

    private File sourceDirectory;

    private List<String> include;

    private List<String> exclude;

    private ExportFormat exportFormat;

    private File template;

    private File output;

    public File getTemplate() {
        return template;
    }

    public File getOutput() {
        return output;
    }

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    public void setTemplate(File template) {
        this.template = template;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }

    public List<String> getInclude() {
        if (include == null) {
            include = new ArrayList<>();
        }
        return include;
    }

    public List<String> getExclude() {
        if (exclude == null) {
            exclude = new ArrayList<>();
        }

        return exclude;
    }
}
