package com.evolveum.midpoint.studio.util;

import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.lang.module.ModuleDescriptor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenUtils {

    public static Set<MavenArtifact> getMidpointDependencies(Project project) {
        return MavenProjectsManager.getInstance(project).getProjects().stream()
                .map(MavenProject::getDependencies)
                .flatMap(List::stream)
                .filter(ma -> ma.getGroupId().startsWith("com.evolveum.midpoint") || ma.getGroupId().startsWith("com.evolveum.prism"))
                .collect(Collectors.toSet());
    }

    public static String getMidpointVersion(Project project) {
        List<String> versions = getMidpointDependencies(project).stream()
                .map(MavenArtifact::getVersion)
                .distinct()
                .sorted()
                .toList();

        if (versions.size() != 1) {
            return null;
        }

        return versions.get(0);
    }

    public static boolean isMidpointVersionGreaterThan(Project project, String version) {
        String currentVersion = getMidpointVersion(project);
        if (currentVersion == null || version == null) {
            return false;
        }

        ModuleDescriptor.Version ver = ModuleDescriptor.Version.parse(version);
        ModuleDescriptor.Version current = ModuleDescriptor.Version.parse(currentVersion);

        return ver.compareTo(current) <= 0;
    }
}
