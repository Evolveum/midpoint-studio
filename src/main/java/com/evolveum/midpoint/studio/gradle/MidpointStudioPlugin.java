package com.evolveum.midpoint.studio.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaPlugin;

import static com.evolveum.midpoint.studio.gradle.Constants.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidpointStudioPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        checkPluginVersion(project);

        project.getPlugins().apply(JavaPlugin.class);

        MidpointStudioPluginExtension extension = project.getExtensions()
                .create(EXTENSION_NAME, MidpointStudioPluginExtension.class);

        configureSetupRepositoriesTask(project, extension);
        configureSetupDependenciesTask(project, extension);
    }

    private void configureSetupRepositoriesTask(Project project, MidpointStudioPluginExtension extension) {
        project.getTasks().register(TASK_SETUP_REPOSITORIES, SetupRepositoriesTask.class).configure((task) -> {

            RepositoryHandler handler = project.getRepositories();

            MavenArtifactRepository nexusPublic = handler.maven(repo -> repo.setUrl(REPOSITORY_EVOLVEUM_URL));
            handler.add(nexusPublic);
        });
    }

    private void configureSetupDependenciesTask(Project project, MidpointStudioPluginExtension extension) {
        project.getTasks().register(TASK_SETUP_DEPENDENCIES, SetupDependenciesTask.class).configure((task) -> {

            Configuration midpointConfiguration = project.getConfigurations().create(CONFIGURATION_NAME);
            midpointConfiguration.setVisible(false);

            // example for now
            Dependency dependency = project.getDependencies().create("org.apache.commons:commons-lang3:3.12.0");
            midpointConfiguration.getDependencies()
                    .add(dependency);


            project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
                    .extendsFrom(midpointConfiguration);

            project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME)
                    .extendsFrom(midpointConfiguration);
        });
    }

    private void checkPluginVersion(Project project) {
        if (!MidpointStudioPluginFeature.SELF_UPDATE_CHECK.isPluginFeatureEnabled(project)) {
            return;
        }

        if (project.getGradle().getStartParameter().isOffline()) {
            return;
        }

        // todo implement
    }
}
