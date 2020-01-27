package com.evolveum.midpoint.philosopher.generator;

import com.beust.jcommander.Parameter;
import com.evolveum.midpoint.philosopher.util.URIConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LocalOptions {

    public static final String P_SOURCE_DIRECTORY = "-s";
    public static final String P_SOURCE_DIRECTORY_LONG = "--source-directory";

    public static final String P_INCLUDE = "-i";
    public static final String P_INCLUDE_LONG = "--include";

    public static final String P_EXCLUDE = "-e";
    public static final String P_EXCLUDE_LONG = "--exclude";

    @Parameter(names = {P_SOURCE_DIRECTORY, P_SOURCE_DIRECTORY_LONG}, validateWith = URIConverter.class, descriptionKey = "local.sourceDirectory")
    private File sourceDirectory;

    @Parameter(names = {P_INCLUDE, P_INCLUDE_LONG}, descriptionKey = "local.include")
    private List<String> include;

    @Parameter(names = {P_EXCLUDE, P_EXCLUDE_LONG}, descriptionKey = "local.exclude")
    private List<String> exclude;

    public File getSourceDirectory() {
        return sourceDirectory;
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

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }
}
