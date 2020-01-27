package com.evolveum.midpoint.philosopher.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.evolveum.midpoint.philosopher.generator.ExportFormat;
import com.evolveum.midpoint.philosopher.generator.GenerateOptions;

import java.io.File;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "generate")
public class CmdGenerateOptions extends GenerateOptions {

    public static final String P_EXPORT_FORMAT = "-ef";
    public static final String P_EXPORT_FORMAT_LONG = "--export-format";

    public static final String P_TEMPLATE = "-t";
    public static final String P_TEMPLATE_LONG = "--template";

    public static final String P_OUTPUT = "-o";
    public static final String P_OUTPUT_LONG = "--output";

    public static final String P_SOURCE_DIRECTORY = "-s";
    public static final String P_SOURCE_DIRECTORY_LONG = "--source-directory";

    public static final String P_INCLUDE = "-i";
    public static final String P_INCLUDE_LONG = "--include";

    public static final String P_EXCLUDE = "-e";
    public static final String P_EXCLUDE_LONG = "--exclude";

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

    @Parameter(names = {P_OUTPUT, P_OUTPUT_LONG}, descriptionKey = "generate.output")
    @Override
    public void setOutput(File output) {
        super.setOutput(output);
    }

    @Parameter(names = {P_SOURCE_DIRECTORY, P_SOURCE_DIRECTORY_LONG}, validateWith = URIConverter.class, descriptionKey = "generate.sourceDirectory")
    @Override
    public void setSourceDirectory(File sourceDirectory) {
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
}
