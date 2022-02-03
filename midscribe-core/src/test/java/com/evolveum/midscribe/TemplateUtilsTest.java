package com.evolveum.midscribe;

import com.evolveum.midpoint.xml.ns._public.common.common_3.CapabilityCollectionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.evolveum.midscribe.generator.Generator;
import com.evolveum.midscribe.generator.GeneratorProperties;
import com.evolveum.midscribe.generator.MidPointObjectStore;
import com.sun.source.tree.AssertTree;
import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TemplateUtilsTest extends MidscribeTest {

    @Test
    public void testDescribeCapability() throws Exception {
        GenerateOptions opts = prepareOptions("mid-7529");
        opts.setInclude(Arrays.asList(new String[]{"mid-7529.xml"}));

        MidPointObjectStore store = opts.getObjectStoreInstance();
        store.init();

        ResourceType resource = store.get(ResourceType.class, "817b95be-ee5e-4322-bc38-6225e77d69b3");
        CapabilityCollectionType capabilities = resource.getCapabilities().getConfigured();

        Map<String, Object> variables = new HashMap<>();
        variables.put("capabilities", capabilities);

        Properties props = new Properties();
        props.setProperty(GeneratorProperties.VELOCITY_START_TEMPLATE, "/template/capabilities.vm");
        props.put(GeneratorProperties.VELOCITY_ADDITIONAL_VARIABLES, variables);

        Generator generator = new Generator(opts);
        generator.generate(props);

        File adoc = opts.getAdocOutput();
        AssertJUnit.assertTrue(adoc.exists());

        assertFilesContentEqual(new File("./src/test/resources/mid-7529/mid-7529.adoc"), adoc);
    }
}
