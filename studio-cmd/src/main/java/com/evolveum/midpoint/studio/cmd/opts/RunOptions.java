package com.evolveum.midpoint.studio.cmd.opts;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.evolveum.midpoint.studio.cmd.util.FileReference;
import com.evolveum.midpoint.studio.cmd.util.FileReferenceConverter;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "run")
public class RunOptions {

    public static final String P_FILE = "-f";
    public static final String P_FILE_LONG = "--file";

    @Parameter(names = {P_FILE, P_FILE_LONG}, descriptionKey = "run.file",
            converter = FileReferenceConverter.class, validateWith = FileReferenceConverter.class)
    private FileReference data;

    public FileReference getData() {
        return data;
    }
}
