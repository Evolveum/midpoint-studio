package com.evolveum.midscribe.generator;

import java.io.File;
import java.io.IOException;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface Exporter {

    void export(File adocFile, File output) throws IOException;
}
