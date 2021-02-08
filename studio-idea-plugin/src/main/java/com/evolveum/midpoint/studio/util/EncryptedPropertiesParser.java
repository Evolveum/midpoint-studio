package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.EncryptedProperty;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EncryptedPropertiesParser {

    private EnvironmentService environmentService;

    public EncryptedPropertiesParser(@NotNull EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    public List<EncryptedProperty> parse(File file) throws IOException {
        // todo implement
        return null;
    }

    protected String mapEnvironment(String envName) {
        // todo implement
        return null;
    }
}
