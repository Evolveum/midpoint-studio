package com.evolveum.midscribe.generator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface GeneratorProperties {

    String VELOCITY_START_TEMPLATE = "velocity.start.template";

    String VELOCITY_START_TEMPLATE_DEFAULT = "/template/documentation.vm";

    String VELOCITY_ADDITIONAL_VARIABLES = "velocity.additional.variables";

    Map<String, Object> VELOCITY_ADDITIONAL_VARIABLES_DEFAULT = new HashMap<>();
}
