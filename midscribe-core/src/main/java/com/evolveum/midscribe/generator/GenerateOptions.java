package com.evolveum.midscribe.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GenerateOptions {

    private List<File> sourceDirectory;

    private List<String> include;

    private List<String> exclude;

    private ExportFormat exportFormat;

    private File template;

    private File adocOutput;

    private File exportOutput;

    private File properties;

    private boolean expand;

    private File expanderProperties;

    private Class<? extends MidPointObjectStore> objectStoreType;

    private MidPointObjectStore objectStoreInstance;

    private Class<? extends TemplateEngineContextBuilder> templateEngineContextBuilder;

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

    public List<File> getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(List<File> sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }

    public Class<? extends MidPointObjectStore> getObjectStoreType() {
        return objectStoreType;
    }

    public void setObjectStoreType(Class<? extends MidPointObjectStore> objectStoreType) {
        this.objectStoreType = objectStoreType;
    }

    public Class<? extends TemplateEngineContextBuilder> getTemplateEngineContextBuilder() {
        return templateEngineContextBuilder;
    }

    public void setTemplateEngineContextBuilder(Class<? extends TemplateEngineContextBuilder> templateEngineContextBuilder) {
        this.templateEngineContextBuilder = templateEngineContextBuilder;
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

    public File getProperties() {
        return properties;
    }

    public void setProperties(File properties) {
        this.properties = properties;
    }

    public boolean isExpand() {
        return expand;
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    public File getExpanderProperties() {
        return expanderProperties;
    }

    public void setExpanderProperties(File expanderProperties) {
        this.expanderProperties = expanderProperties;
    }

    public MidPointObjectStore getObjectStoreInstance() {
        return objectStoreInstance;
    }

    public void setObjectStoreInstance(MidPointObjectStore objectStoreInstance) {
        this.objectStoreInstance = objectStoreInstance;
    }
}
