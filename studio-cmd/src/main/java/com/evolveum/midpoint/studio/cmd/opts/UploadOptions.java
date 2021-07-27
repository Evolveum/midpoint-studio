/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.studio.cmd.opts;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.evolveum.midpoint.studio.cmd.util.FileReference;
import com.evolveum.midpoint.studio.cmd.util.FileReferenceConverter;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "upload")
public class UploadOptions extends BaseOptions {

    public static final String P_DATA = "-d";
    public static final String P_DATA_LONG = "--data";

    public static final String P_RAW = "-r";
    public static final String P_RAW_LONG = "--raw";

    public static final String P_IMPORT = "-i";
    public static final String P_IMPORT_LONG = "--import";

    public static final String P_OVERWRITE = "-o";
    public static final String P_OVERWRITE_LONG = "--overwrite";

    @Parameter(names = {P_DATA, P_DATA_LONG}, descriptionKey = "upload.data",
            converter = FileReferenceConverter.class, validateWith = FileReferenceConverter.class)
    private FileReference data;

    @Parameter(names = {P_RAW, P_RAW_LONG}, descriptionKey = "upload.raw")
    private boolean raw;

    @Parameter(names = {P_IMPORT, P_IMPORT_LONG}, descriptionKey = "upload.import")
    private boolean _import;

    @Parameter(names = {P_OVERWRITE, P_OVERWRITE_LONG}, descriptionKey = "upload.overwrite")
    private boolean overwrite;

    public FileReference getData() {
        return data;
    }

    public boolean isImport() {
        return _import;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public boolean isRaw() {
        return raw;
    }
}
