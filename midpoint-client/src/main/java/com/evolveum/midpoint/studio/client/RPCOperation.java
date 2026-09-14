/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.client;

import com.evolveum.midpoint.model.api.util.ConnectorGeneratorConstants;
import com.evolveum.midpoint.model.api.util.SmartIntegrationConstants;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SmartIntegrationOperationStatusInfoType;

/**
 * Defines the RPC operations associates each operation with its corresponding status information, submit operation, and
 * response type.
 *
 * <p>Each enum constant represents one logical RPC operation and provides the
 * information required to invoke the operation and process its response.</p>
 *
 */
public enum RPCOperation {

    /**
     * RPC operations for Smart Integration service.
     */
    RPC_SUGGEST_OBJECT_TYPES(
            SmartIntegrationConstants.RPC_SUGGEST_OBJECT_TYPES_SUBMIT_OPERATION,
            SmartIntegrationConstants.RPC_SUGGEST_OBJECT_TYPES_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_SUGGEST_CORRELATIONS(
            SmartIntegrationConstants.RPC_SUGGEST_CORRELATIONS_SUBMIT_OPERATION,
            SmartIntegrationConstants.RPC_SUGGEST_CORRELATIONS_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_SUGGEST_MAPPINGS(
            SmartIntegrationConstants.RPC_SUGGEST_MAPPINGS_SUBMIT_OPERATION,
            SmartIntegrationConstants.RPC_SUGGEST_MAPPINGS_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_SUGGEST_FOCUS_TYPE(
            SmartIntegrationConstants.RPC_SUGGEST_FOCUS_TYPE_SUBMIT_OPERATION,
            SmartIntegrationConstants.RPC_SUGGEST_FOCUS_TYPE_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_SUGGEST_ASSOCIATION_TYPE(
            SmartIntegrationConstants.RPC_SUGGEST_ASSOCIATION_TYPE_SUBMIT_OPERATION,
            SmartIntegrationConstants.RPC_SUGGEST_ASSOCIATION_TYPE_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    /**
     * RPC operations for Connector Generator service.
     */
    RPC_START_FROM_NEW(
            null,
            ConnectorGeneratorConstants.RPC_START_FROM_NEW,
            ConnectorDevelopmentType.class
    ),

    RPC_CONTINUE_FROM(
            ConnectorGeneratorConstants.RPC_CONTINUE_FROM,
            null,
            ConnectorDevelopmentType.class
    ),

    RPC_CREATE_CONNECTOR(
            ConnectorGeneratorConstants.RPC_CREATE_CONNECTOR_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_CREATE_CONNECTOR_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_DISCOVER_BASIC_INFORMATION(
            ConnectorGeneratorConstants.RPC_DISCOVER_BASIC_INFORMATION_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_DISCOVER_BASIC_INFORMATION_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_DISCOVER_DOCUMENTATION(
            ConnectorGeneratorConstants.RPC_DISCOVER_DOCUMENTATION_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_DISCOVER_DOCUMENTATION_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_PROCESS_DOCUMENTATION(
            ConnectorGeneratorConstants.RPC_PROCESS_DOCUMENTATION_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_PROCESS_DOCUMENTATION_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_GENERATE_ARTIFACT(
            ConnectorGeneratorConstants.RPC_GENERATE_ARTIFACT_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_GENERATE_ARTIFACT_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_DISCOVER_CONNECTIVITY_ENDPOINT(
            ConnectorGeneratorConstants.RPC_DISCOVER_CONNECTIVITY_ENDPOINT_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_DISCOVER_CONNECTIVITY_ENDPOINT_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_DISCOVER_OBJECT_CLASSES(
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASSES_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASSES_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_DISCOVER_OBJECT_CLASS_INFORMATION(
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASS_INFORMATION_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASS_INFORMATION_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_DISCOVER_OBJECT_CLASS_ATTRIBUTES(
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASS_ATTRIBUTES_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASS_ATTRIBUTES_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_DISCOVER_OBJECT_CLASS_ENDPOINTS(
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASS_ENDPOINTS_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_DISCOVER_OBJECT_CLASS_ENDPOINTS_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_REFRESH_SCHEMA(
            ConnectorGeneratorConstants.RPC_REFRESH_SCHEMA_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_REFRESH_SCHEMA_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_GENERATE_NATIVE_SCHEMA(
            ConnectorGeneratorConstants.RPC_GENERATE_NATIVE_SCHEMA_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_GENERATE_ARTIFACT_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    ),

    RPC_GENERATE_AUTHENTICATION_SCRIPT(
            ConnectorGeneratorConstants.RPC_GENERATE_AUTHENTICATION_SCRIPT_SUBMIT_OPERATION,
            ConnectorGeneratorConstants.RPC_GENERATE_ARTIFACT_STATUS_INFO,
            SmartIntegrationOperationStatusInfoType.class
    );

    private final String statusInfoConstant;
    private final String submitOperationConstant;
    private final Class<?> responseType;

    RPCOperation(
            String statusInfoConstant,
            String submitOperationConstant,
            Class<?> responseType
    ) {
        this.statusInfoConstant = statusInfoConstant;
        this.submitOperationConstant = submitOperationConstant;
        this.responseType = responseType;
    }

    public String getStatusInfoConstant() {
        return statusInfoConstant;
    }

    public String getSubmitOperationConstant() {
        return submitOperationConstant;
    }

    public Class<?> getResponseType() {
        return responseType;
    }
}