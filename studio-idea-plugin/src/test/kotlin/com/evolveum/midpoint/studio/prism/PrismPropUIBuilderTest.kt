/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.prism

import com.evolveum.midpoint.prism.PrismContainerDefinition
import com.evolveum.midpoint.prism.PrismContext
import com.evolveum.midpoint.schema.constants.SchemaConstants
import com.evolveum.midpoint.schema.util.ConnectorTypeUtil
import com.evolveum.midpoint.studio.ui.prism.PrismDefinitionPanelBuilder
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PrismPropUIBuilderTest: BasePlatformTestCase() {

    private lateinit var prismContext: PrismContext
    private lateinit var builder: PrismDefinitionPanelBuilder
    private lateinit var connector: ConnectorType

    override fun setUp() {
        super.setUp()
        prismContext = PrismContext.get()
        builder = PrismDefinitionPanelBuilder()
        connector = initConnectorTest()
    }

    @Throws(Exception::class)
    private fun initConnectorTest() : ConnectorType {

        val resourcePath = "/object/connectors/openproject-connector-test.xml"
        val inputStream = javaClass.getResourceAsStream(resourcePath)

        inputStream.use { input ->

            // Parse XML into PrismObject<ConnectorType>
            val connectorPrismObject =
                prismContext.parserFor(input)
                    .xml()
                    .parse<ConnectorType>()

            val connectorType = connectorPrismObject.asObjectable()

            // Basic verification
            check(connectorType.oid != null) {
                "Connector oid is missing"
            }

            check(connectorType.name != null) {
                "Connector name is missing"
            }

            return connectorType
        }
    }

    fun testConnectorConfigurationContainerDefinitionUIBuild() {

        try {
            val connectorConfigurationContainerDefinition: PrismContainerDefinition<*>? =
                ConnectorTypeUtil.parseConnectorSchemaIfPresent(connector)
                ?.connectorConfigurationContainerDefinition
                ?.findLocalItemDefinition(SchemaConstants.ICF_CONFIGURATION_PROPERTIES_NAME)

            if (connectorConfigurationContainerDefinition != null) {
                val panel = builder.build(connectorConfigurationContainerDefinition)
                assertNotNull(panel)
            } else {
                throw NullPointerException("Connector configuration is missing.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            fail("Failed to build UI panel")
        }
    }
}