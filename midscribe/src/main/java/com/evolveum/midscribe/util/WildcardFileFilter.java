package com.evolveum.midscribe.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class WildcardFileFilter implements FileFilter {

    private List<PathMatcher> matchers = new ArrayList<>();

    public WildcardFileFilter(List<String> wildcards) {
        for (String wildcard : wildcards) {
            String path = normalizePath(wildcard);
            matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + path));
        }
    }

    private String normalizePath(String path) {
        path = new File(path).getPath();
        if ("\\".equals(File.separator)) {
            path = path.replace("\\", "\\\\");
        }

        return path;
    }

    @Override
    public boolean accept(File file) {
        for (PathMatcher matcher : matchers) {
            if (matcher.matches(file.toPath())) {
                return true;
            }
        }

        return false;
    }
}
