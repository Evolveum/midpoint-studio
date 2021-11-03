package com.evolveum.midscribe.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class WildcardFileFilter implements FileFilter {

    private List<PathMatcher> matchers = new ArrayList<>();

    public WildcardFileFilter(List<String> wildcards) {
        for (String wildcard : wildcards) {
            matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + wildcard));
        }
    }

    @Override
    public boolean accept(File file) {
        for (PathMatcher matcher : matchers) {
            if (  matcher.matches(file.toPath())) {
                return true;
            }
        }

        return false;
    }
}
