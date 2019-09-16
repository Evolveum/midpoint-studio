package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.util.Localized;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum Generate implements Localized {

    BULK_RECOMPUTE("Bulk action: Recompute"),
    BULK_ENABLE("Bulk action: Enable"),
    BULK_DISABLE("Bulk action: Disable"),
    BULK_DELETE("Bulk action: Delete"),
    BULK_MODIFY("Bulk action: Modify"),
    BULK_ASSIGN_SOMETHING("Bulk action: Assign (something) to objects"),
    BULK_ASSIGN_OBJECTS("Bulk action: Assign objects to (something)"),
    BULK_EXECUTE_SCRIPT("Bulk action: Execute script"),
    BULK_SEND_NOTIFICATIONS("Bulk action: Send notifications"),
    BULK_LOG("Bulk action: Log"),
    BULK_TEST_RESOURCE("Bulk action: Test resource"),
    BULK_VALIDATE_RESOURCE("Bulk action: Validate resource"),
    NATIVE_RECOMPUTE("Native task: Recompute"),
    NATIVE_DELETE("Native task: Delete"),
    NATIVE_MODIFY("Native task: Modify (execute changes)"),
    NATIVE_CHECK_SHADOW_INTEGRITY("Native task: Check shadow integrity"),
    QUERY("Query returning objects"),
    ASSIGNMENT("Assignment"),
    REFERENCE_TARGET_REF("Reference (targetRef)"),
    REFERENCE_RESOURCE_REF("Reference (resourceRef)"),
    REFERENCE_LINK_REF("Reference (linkRef)"),
    REFERENCE_CONNECTOR_REF("Reference (connectorRef)"),
    REFERENCE_PARENT_ORG_REF("Reference (parentOrgRef)"),
    REFERENCE_OWNER_REF("Reference (ownerRef)");

    private String key;

    Generate(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
