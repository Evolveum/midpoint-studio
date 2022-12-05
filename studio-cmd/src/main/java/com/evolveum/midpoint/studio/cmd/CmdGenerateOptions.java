package com.evolveum.midpoint.studio.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.evolveum.midscribe.generator.ExportFormat;
import com.evolveum.midscribe.generator.GenerateOptions;

import java.io.File;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "generate")
public class CmdGenerateOptions extends GenerateOptions {

    public static final String P_SOURCE_DIRECTORY = "-s";
    public static final String P_SOURCE_DIRECTORY_LONG = "--source-directory";

    public static final String P_INCLUDE = "-i";
    public static final String P_INCLUDE_LONG = "--include";

    public static final String P_EXCLUDE = "-e";
    public static final String P_EXCLUDE_LONG = "--exclude";

    public static final String P_EXPORT_FORMAT = "-ef";
    public static final String P_EXPORT_FORMAT_LONG = "--export-format";

    public static final String P_TEMPLATE = "-t";
    public static final String P_TEMPLATE_LONG = "--template";

    public static final String P_ADOC_OUTPUT = "-ao";
    public static final String P_ADOC_OUTPUT_LONG = "--adoc-output";

    public static final String P_EXPORT_OUTPUT = "-eo";
    public static final String P_EXPORT_OUTPUT_LONG = "--export-output";

    public static final String P_PROPERTIES_FILE = "-pf";
    public static final String P_PROPERTIES_FILE_LONG = "--properties-file";

    @Parameter(names = {P_TEMPLATE, P_TEMPLATE_LONG}, descriptionKey = "generate.template")
    @Override
    public void setTemplate(File template) {
        super.setTemplate(template);
    }

    @Parameter(names = {P_EXPORT_FORMAT, P_EXPORT_FORMAT_LONG}, descriptionKey = "generate.exportFormat")
    @Override
    public void setExportFormat(ExportFormat exportFormat) {
        super.setExportFormat(exportFormat);
    }

    @Parameter(names = {P_EXPORT_OUTPUT, P_EXPORT_OUTPUT_LONG}, descriptionKey = "generate.exportOutput")
    @Override
    public void setExportOutput(File exportOutput) {
        super.setExportOutput(exportOutput);
    }

    @Parameter(names = {P_SOURCE_DIRECTORY, P_SOURCE_DIRECTORY_LONG}, converter = FileConverter.class, validateWith = FileConverter.class, descriptionKey = "generate.sourceDirectory")
    @Override
    public void setSourceDirectory(List<File> sourceDirectory) {
        super.setSourceDirectory(sourceDirectory);
    }

    @Parameter(names = {P_EXCLUDE, P_EXCLUDE_LONG}, descriptionKey = "generate.exclude")
    @Override
    public void setExclude(List<String> exclude) {
        super.setExclude(exclude);
    }

    @Parameter(names = {P_INCLUDE, P_INCLUDE_LONG}, descriptionKey = "generate.include")
    @Override
    public void setInclude(List<String> include) {
        super.setInclude(include);
    }

    @Parameter(names = {P_ADOC_OUTPUT, P_ADOC_OUTPUT_LONG}, descriptionKey = "generate.adocOutput")
    @Override
    public void setAdocOutput(File adocOutput) {
        super.setAdocOutput(adocOutput);
    }

    @Parameter(names = {P_PROPERTIES_FILE, P_PROPERTIES_FILE_LONG}, descriptionKey = "generate.propertiesFile")
    @Override
    public void setProperties(File properties) {
        super.setProperties(properties);
    }
}
