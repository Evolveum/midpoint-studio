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
public class UploadOptions {

    public static final String P_DATA = "-d";
    public static final String P_DATA_LONG = "--data";

    public static final String P_RECOMPUTE = "-r";
    public static final String P_RECOMPUTE_LONG = "--recompute";

    public static final String P_TEST_RESOURCE = "-t";
    public static final String P_TEST_RESOURCE_LONG = "--test-resource";

    public static final String P_VALIDATE_RESOURCE = "-v";
    public static final String P_VALIDATE_RESOURCE_LONG = "--validate-resource";

    public static final String P_STOP_ON_ERROR = "-s";
    public static final String P_STOP_ON_ERROR_LONG = "--stop-on-error";

    @Parameter(names = {P_DATA, P_DATA_LONG}, descriptionKey = "upload.data",
            converter = FileReferenceConverter.class, validateWith = FileReferenceConverter.class)
    private FileReference data;

    @Parameter(names = {P_RECOMPUTE, P_RECOMPUTE_LONG}, descriptionKey = "upload.recompute")
    private boolean recompute;

    @Parameter(names = {P_TEST_RESOURCE, P_TEST_RESOURCE_LONG}, descriptionKey = "upload.testResource")
    private boolean testResource;

    @Parameter(names = {P_VALIDATE_RESOURCE, P_VALIDATE_RESOURCE_LONG}, descriptionKey = "upload.validateResource")
    private boolean validateResource;

    @Parameter(names = {P_STOP_ON_ERROR, P_STOP_ON_ERROR_LONG}, descriptionKey = "upload.stopOnError")
    private boolean stopOnError;

    public FileReference getData() {
        return data;
    }

    public boolean isRecompute() {
        return recompute;
    }

    public boolean isTestResource() {
        return testResource;
    }

    public boolean isValidateResource() {
        return validateResource;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }
}
