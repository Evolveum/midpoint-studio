package com.evolveum.midpoint.studio.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.util.internal.VersionNumber;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

import static com.evolveum.midpoint.studio.gradle.Constants.*;
import static com.evolveum.midpoint.studio.gradle.Utils.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidpointStudioPlugin implements Plugin<Project> {

    private String context;

    @Override
    public void apply(@Nonnull Project project) {
        context = getContext(project);

        MidpointStudioPluginExtension extension = project.getExtensions()
                .create(EXTENSION_NAME, MidpointStudioPluginExtension.class);

        checkPluginVersion(project, extension);

        project.getPlugins().apply(JavaPlugin.class);

        configureSetupRepositoriesTask(project, extension);
        configureSetupDependenciesTask(project, extension);
    }

    private void configureSetupRepositoriesTask(Project project, MidpointStudioPluginExtension extension) {
        info(context, "Configuring setup repositories task");

        project.getTasks().register(TASK_SETUP_REPOSITORIES, SetupRepositoriesTask.class).configure((task) -> {

            RepositoryHandler handler = project.getRepositories();

            MavenArtifactRepository nexusPublic = handler.maven(repo -> repo.setUrl(REPOSITORY_EVOLVEUM_URL));
            handler.add(nexusPublic);
        });
    }

    private void configureSetupDependenciesTask(Project project, MidpointStudioPluginExtension extension) {
        info(context, "Configuring setup dependencies task");

        project.getTasks().register(TASK_SETUP_DEPENDENCIES, SetupDependenciesTask.class).configure((task) -> {

            info(context, "Midpoint version: " + extension.getMidpointVersion());

            Configuration midpointConfiguration = project.getConfigurations().create(CONFIGURATION_NAME);
            midpointConfiguration.setVisible(false);

            addDependency(project, midpointConfiguration, "com.evolveum.midpoint.model:model-impl:" + extension.getMidpointVersion());

            project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
                    .extendsFrom(midpointConfiguration);

            project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME)
                    .extendsFrom(midpointConfiguration);
        });
    }

    private void addDependency(Project project, Configuration midpoint, String dependencyDescription) {
        Dependency dependency = project.getDependencies().create(dependencyDescription);
        midpoint.getDependencies().add(dependency);
    }

    private void checkPluginVersion(Project project, MidpointStudioPluginExtension extension) {
        if (!MidpointStudioPluginFeature.SELF_UPDATE_CHECK.isPluginFeatureEnabled(project)) {
            return;
        }

        if (project.getGradle().getStartParameter().isOffline()) {
            return;
        }

        if (!extension.isCheckPluginUpdates()) {
            return;
        }

        VersionNumber current = getCurrentVersion();
        VersionNumber latest = getLatestVersion();

        if (current != null && latest != null && current.compareTo(latest) < 0) {
            warn(context, PLUGIN_NAME + " Studio is outdated: " + current + ". Update " + PLUGIN_ID + " to: " + latest);
        }
    }

    private VersionNumber getLatestVersion() {
        // todo implement
        return null;
    }

    private VersionNumber getCurrentVersion() {
        URL url = MidpointStudioPlugin.class.getResource("$" + MidpointStudioPlugin.class.getSimpleName() + ".class");
        if (url == null) {
            return null;
        }

        String path = url.getPath();
        if (path.startsWith("file:")) {
            path = "jar:" + path;
        }

        String manifestPath = path.substring(0, path.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        info(context, "Resolving Midpoint Studio plugin version with: " + manifestPath);

        try (InputStream is = new URL(manifestPath).openStream()) {
            Manifest manifest = new Manifest(is);
            String version = manifest.getMainAttributes().getValue("Version");

            return VersionNumber.parse(version);
        } catch (Exception ex) {
        }

        return null;
    }
}
