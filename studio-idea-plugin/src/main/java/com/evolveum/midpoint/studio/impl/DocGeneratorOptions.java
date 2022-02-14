package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.util.FileConverter;
import com.evolveum.midscribe.generator.ExportFormat;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.OptionTag;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DocGeneratorOptions {

    @OptionTag(converter = FileConverter.class)
    private File sourceDirectory;

    private List<String> include;

    private List<String> exclude;

    private ExportFormat exportFormat;

    @OptionTag(converter = FileConverter.class)
    private File template;

    @OptionTag(converter = FileConverter.class)
    private File adocOutput;

    @OptionTag(converter = FileConverter.class)
    private File exportOutput;

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public List<String> getInclude() {
        if (include == null) {
            include = new ArrayList<>();
        }
        return include;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public List<String> getExclude() {
        if (exclude == null) {
            exclude = new ArrayList<>();
        }
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    public File getTemplate() {
        return template;
    }

    public void setTemplate(File template) {
        this.template = template;
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

    public void setExportOutput(File exportOutput) {
        this.exportOutput = exportOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocGeneratorOptions that = (DocGeneratorOptions) o;

        if (sourceDirectory != null ? !sourceDirectory.equals(that.sourceDirectory) : that.sourceDirectory != null)
            return false;
        if (include != null ? !include.equals(that.include) : that.include != null) return false;
        if (exclude != null ? !exclude.equals(that.exclude) : that.exclude != null) return false;
        if (exportFormat != that.exportFormat) return false;
        if (template != null ? !template.equals(that.template) : that.template != null) return false;
        if (adocOutput != null ? !adocOutput.equals(that.adocOutput) : that.adocOutput != null) return false;
        return exportOutput != null ? exportOutput.equals(that.exportOutput) : that.exportOutput == null;
    }

    @Override
    public int hashCode() {
        int result = sourceDirectory != null ? sourceDirectory.hashCode() : 0;
        result = 31 * result + (include != null ? include.hashCode() : 0);
        result = 31 * result + (exclude != null ? exclude.hashCode() : 0);
        result = 31 * result + (exportFormat != null ? exportFormat.hashCode() : 0);
        result = 31 * result + (template != null ? template.hashCode() : 0);
        result = 31 * result + (adocOutput != null ? adocOutput.hashCode() : 0);
        result = 31 * result + (exportOutput != null ? exportOutput.hashCode() : 0);
        return result;
    }

    public static DocGeneratorOptions createDefaultOptions(Project project) {
        DocGeneratorOptions opts = new DocGeneratorOptions();
        opts.setSourceDirectory(new File(project.getBasePath(), "objects"));
        opts.setInclude(Arrays.asList("**/*.xml"));
        opts.setExportFormat(ExportFormat.HTML);
        opts.setExportOutput(new File(project.getBasePath(), "documentation.html"));

        return opts;
    }

    public static GenerateOptions buildGenerateOptions(DocGeneratorOptions opts) {
        GenerateOptions go = new GenerateOptions();
        go.setAdocOutput(opts.getAdocOutput());
        go.setExportFormat(opts.getExportFormat());
        go.setTemplate(opts.getTemplate());
        go.setExportOutput(opts.getExportOutput());
        go.setSourceDirectory(opts.getSourceDirectory());
        go.setInclude(opts.getInclude());
        go.setExclude(opts.getExclude());

        return go;
    }

    public static DocGeneratorOptions buildDocGenerateOptions(GenerateOptions opts) {
        DocGeneratorOptions dgo = new DocGeneratorOptions();

        dgo.setSourceDirectory(opts.getSourceDirectory());
        dgo.setInclude(opts.getInclude());
        dgo.setExclude(opts.getExclude());
        dgo.setExportFormat(opts.getExportFormat());
        dgo.setTemplate(opts.getTemplate());
        dgo.setAdocOutput(opts.getAdocOutput());
        dgo.setExportOutput(opts.getExportOutput());

        return dgo;
    }
}
