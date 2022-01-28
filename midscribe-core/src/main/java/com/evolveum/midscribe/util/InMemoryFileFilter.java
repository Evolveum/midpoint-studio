package com.evolveum.midscribe.util;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryFileFilter implements IOFileFilter {

    private WildcardFileFilter include;

    private WildcardFileFilter exclude;

    public InMemoryFileFilter(File base, List<String> includes, List<String> excludes) {
        if (includes == null || includes.isEmpty()) {
            includes = Arrays.asList("**.[xX][mM][lL]");
        }
        include = buildFilter(base, includes);
        exclude = buildFilter(base, excludes);
    }

    private WildcardFileFilter buildFilter(File base, List<String> wildcards) {
        if (wildcards == null || wildcards.isEmpty()) {
            return null;
        }

        List<String> fullPathWildcards = wildcards.stream().map(wildcard -> base.getPath() + File.separator + wildcard).collect(Collectors.toList());

        return new WildcardFileFilter(fullPathWildcards);
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
