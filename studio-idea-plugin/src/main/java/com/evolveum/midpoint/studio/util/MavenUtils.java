package com.evolveum.midpoint.studio.util;

import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenUtils {

    public static Set<MavenArtifact> getMidpointDependencies(Project project) {
        return MavenProjectsManager.getInstance(project).getProjects().stream()
                .map(p -> p.getDependencies())
                .flatMap(List::stream)
                .filter(ma -> ma.getGroupId().startsWith("com.evolveum.midpoint") || ma.getGroupId().startsWith("com.evolveum.prism"))
                .collect(Collectors.toSet());
    }
}
