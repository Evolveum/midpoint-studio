package com.evolveum.midpoint.studio;

import java.util.List;

/**
 * Created by lazyman on 10/02/2017.
 */
public interface MidPointConstants {

    String PLUGIN_ID = "com.evolveum.midpoint.studio";

    String PLUGIN_NAME = "MidPoint.Plugin";

    String ACTION_ID_PREFIX = "MidPoint.Action.";

    String DEFAULT_MIDPOINT_VERSION = "4.8";

    List<String> SUPPORTED_VERSIONS = List.of("4.4", "4.5", "4.6", "4.7", "4.8", "4.9");
}
