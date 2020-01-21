package com.evolveum.midpoint.philosopher;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "generate")
public class GenerateOptions {

    public static final String P_TEMPLATE = "-template";
    public static final String P_TEMPLATE_LONG = "--template";

    @Parameter(names = {P_TEMPLATE, P_TEMPLATE_LONG}, descriptionKey = "generate.template")
    private File dbPath = new File("./data");

}
