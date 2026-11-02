package com.evolveum.midpoint.studio.impl.configuration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Default settings must not carry a random project id, otherwise every copy of the defaults
 * is unique and equality based checks (settings page modified, state comparison) always fail.
 */
public class MidPointConfigurationTest {

    @Test
    public void testDefaultSettingsHaveNoProjectId() {
        assertNull(MidPointConfiguration.createDefaultSettings().getProjectId());
    }

    @Test
    public void testTwoDefaultSettingsAreEqual() {
        assertEquals(MidPointConfiguration.createDefaultSettings(), MidPointConfiguration.createDefaultSettings());
    }
}
