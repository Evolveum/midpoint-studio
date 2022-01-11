package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.gui.test.TestMidPointSpringApplication;
import com.evolveum.midpoint.model.test.AbstractModelIntegrationTest;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemObjectsType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testng.AssertJUnit;

/**
 * Created by Viliam Repan (lazyman).
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(classes = TestMidPointSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "server.port=18088")
public class MidPointTest extends AbstractModelIntegrationTest {

    @Override
    public void initSystem(Task initTask, OperationResult initResult) throws Exception {
        logger.info("before super init");
        super.initSystem(initTask, initResult);
        logger.info("after super init");
    }

    @Test
    public void simpleTest() throws Exception {
        Environment env = Environment.DEFAULT;
        env.setUrl("http://localhost:18088/midpoint");
        MidPointClient cli = new MidPointClient(null, env);
        MidPointObject obj = cli.get(SystemConfigurationType.class, SystemObjectsType.SYSTEM_CONFIGURATION.value(), null);

        AssertJUnit.assertNotNull(obj);
    }
}
