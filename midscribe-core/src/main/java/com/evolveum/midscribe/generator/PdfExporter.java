package com.evolveum.midscribe.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PdfExporter implements Exporter {

    private static final Logger LOG = LoggerFactory.getLogger(PdfExporter.class);

    @Override
    public String getDefaultExtension() {
        return "pdf";
    }

    @Override
    public void export(File adocFile, File output) throws IOException {
        LOG.error("Not implemented yet");
    }
}
