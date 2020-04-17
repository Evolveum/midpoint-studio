package com.evolveum.midscribe.generator;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CompositeResourceLoader extends ResourceLoader {

    private ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader();

    private File template;

    private Map<String, byte[]> templateMap = new HashMap<>();

    public CompositeResourceLoader(File template) {
        this.template = template;
    }

    @Override
    public void commonInit(RuntimeServices rs, ExtProperties configuration) {
        super.commonInit(rs, configuration);

        classpathResourceLoader.commonInit(rs, configuration);
    }

    @Override
    public void init(ExtProperties configuration) {
        if (template == null) {
            return;
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(template))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String name = zipEntry.getName();

                templateMap.put(name, zis.readAllBytes());
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't load template " + template, ex);
        }
    }

    @Override
    public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException {
        if (!templateMap.containsKey(source)) {
            return classpathResourceLoader.getResourceReader(source, encoding);
        }

        byte[] array = templateMap.get(source);
        try {
            return new InputStreamReader(new ByteArrayInputStream(array), encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new ResourceNotFoundException(ex);
        }
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0;
    }
}
