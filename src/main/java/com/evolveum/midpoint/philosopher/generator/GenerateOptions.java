package com.evolveum.midpoint.philosopher.generator;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "generate")
public class GenerateOptions {

    public static final String P_EXPORT_FORMAT = "-ef";
    public static final String P_EXPORT_FORMAT_LONG = "--export-format";

    public static final String P_TEMPLATE = "-t";
    public static final String P_TEMPLATE_LONG = "--template";

    public static final String P_OUTPUT = "-o";
    public static final String P_OUTPUT_LONG = "--output";

    public static final String P_USE_REMOTE = "-r";
    public static final String P_USE_REMOTE_LONG = "--remote";

    @Parameter(names = {P_USE_REMOTE, P_USE_REMOTE_LONG}, descriptionKey = "generate.useRemote")
    private boolean useRemote;

    @ParametersDelegate
    private RemoteOptions remote;

    @ParametersDelegate
    private LocalOptions local;

    @Parameter(names = {P_EXPORT_FORMAT, P_EXPORT_FORMAT_LONG}, descriptionKey = "generate.exportFormat")
    private ExportFormat exportFormat;

    @Parameter(names = {P_TEMPLATE, P_TEMPLATE_LONG}, descriptionKey = "generate.template")
    private File template;

    @Parameter(names = {P_OUTPUT, P_OUTPUT_LONG}, descriptionKey = "generate.output")
    private File output;

    public RemoteOptions getRemote() {
        return remote;
    }

    public LocalOptions getLocal() {
        return local;
    }

    public File getTemplate() {
        return template;
    }

    public File getOutput() {
        return output;
    }

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setLocal(LocalOptions local) {
        this.local = local;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    public void setRemote(RemoteOptions remote) {
        this.remote = remote;
    }

    public void setTemplate(File template) {
        this.template = template;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public boolean isUseRemote() {
        return useRemote;
    }

    public void setUseRemote(boolean useRemote) {
        this.useRemote = useRemote;
    }
}
