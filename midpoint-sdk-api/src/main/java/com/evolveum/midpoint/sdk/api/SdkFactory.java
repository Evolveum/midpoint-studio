package com.evolveum.midpoint.sdk.api;

import com.google.common.collect.ImmutableList;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SdkFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SdkFactory.class);

    private File sdkFile;

    public SdkFactory sdkFile(File sdkFile) {
        this.sdkFile = sdkFile;
        return this;
    }

    public SdkContext build() throws SdkException {
        Validate.notNull(sdkFile, "SDK jar file must be set");

        if (!sdkFile.exists() || !sdkFile.canRead() || !sdkFile.isFile()) {
            throw new IllegalArgumentException("SDK jar file does not exist or is not readable");
        }

        try {
            Archive archive = new JarFileArchive(sdkFile);

            List<URL> urls = new ArrayList<>();
            urls.add(archive.getUrl());

            Iterator<Archive> iterator = archive.getNestedArchives(null, (entry) -> {
                if (entry.isDirectory()) {
                    return entry.getName().equals("BOOT-INF/classes/");
                }
                return entry.getName().startsWith("BOOT-INF/lib/");
            });

            ImmutableList.copyOf(iterator).forEach(a -> {
                try {
                    urls.add(a.getUrl());
                } catch (MalformedURLException e) {
                    e.printStackTrace();    // todo fix
                }
            });

            LaunchedURLClassLoader classLoader = new LaunchedURLClassLoader(
                    false, archive, urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());

            @SuppressWarnings("unchecked")
            Class<? extends SdkContextFactory> factoryClass = (Class<? extends SdkContextFactory>)
                    scanClasses(classLoader, SdkComponent.class, "com.evolveum.midpoint.sdk")
                            .stream()
                            .filter(c -> Objects.equals(c.getAnnotation(SdkComponent.class).type(), SdkContextFactory.class))
                            .filter(c -> SdkContextFactory.class.isAssignableFrom(c))
                            .findFirst()
                            .orElse(null);

            if (factoryClass == null) {
                throw new SdkException("Couldn't find SDK context factory class annotated with @SdkComponent");
            }

            SdkContextFactory factory = factoryClass.getConstructor().newInstance();
            return factory.creatContext();
        } catch (Exception ex) {
            throw new SdkException("Couldn't build SDK context", ex);
        }
    }

    Collection<Class<?>> scanClasses(
            ClassLoader classLoader, Class<? extends Annotation> annotationClass, String... packageNames) {

        LOG.debug("Scanning classes for: {} with package scope: {}", annotationClass, packageNames);

        try (ScanResult scanResult = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .acceptPackages(packageNames)
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {

            List<Class<?>> classes = scanResult
                    .getClassesWithAnnotation(annotationClass)
                    .loadClasses();

            LOG.debug("Found {} classes with annotation {}", classes.size(), annotationClass.getName());

            return classes;
        }
    }
}
