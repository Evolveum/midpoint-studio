package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.schema.constants.ObjectTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConfiguration {

    Module midpointModule;

    String downloadFilePattern;

    String generatedFilePattern;

    Integer restClientTimeout = 60;

    Boolean restLogCommunication = false;

    List<ObjectTypes> downloadTypesInclude = new ArrayList<>();

    List<ObjectTypes> downloadTypesExclude = new ArrayList<>();

    Integer downloadLimit = 100;

}
