package com.evolveum.midpoint.philosopher.util;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryFileFilter implements IOFileFilter {

    private WildcardFileFilter include;

    private WildcardFileFilter exclude;

    public InMemoryFileFilter(List<String> includes, List<String> excludes) {
        if (includes == null || includes.isEmpty()) {
            includes = Arrays.asList("*.xml");
        }
        include = buildFilter(includes);
        exclude = buildFilter(excludes);
    }

    private WildcardFileFilter buildFilter(List<String> wildcards) {
        if (wildcards == null || wildcards.isEmpty()) {
            return null;
        }

        return new WildcardFileFilter(wildcards);
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return exclude != null ? !exclude.accept(file) : true;
        }

        boolean included = include != null ? include.accept(file) : true;
        if (!included) {
            return false;
        }

        return exclude != null ? !exclude.accept(file) : true;
    }

    @Override
    public boolean accept(File dir, String name) {
        return accept(new File(dir, name));
    }
}
