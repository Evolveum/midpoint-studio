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

    private File adocOutput;

    private File exportOutput;

    private Class<? extends MidPointClient> midpointClient;

    public File getTemplate() {
        return template;
    }

    public File getAdocOutput() {
        return adocOutput;
    }

    public void setAdocOutput(File adocOutput) {
        this.adocOutput = adocOutput;
    }

    public File getExportOutput() {
        return exportOutput;
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

    public void setExportOutput(File exportOutput) {
        this.exportOutput = exportOutput;
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

    public Class<? extends MidPointClient> getMidpointClient() {
        return midpointClient;
    }

    public void setMidpointClient(Class<? extends MidPointClient> midpointClient) {
        this.midpointClient = midpointClient;
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
