package com.evolveum.midpoint.studio.util;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum SyncAction {

    LINK("http://midpoint.evolveum.com/xml/ns/public/model/action-3#link"),

    ADD_FOCUS("http://midpoint.evolveum.com/xml/ns/public/model/action-3#addFocus"),

    DELETE_FOCUS("http://midpoint.evolveum.com/xml/ns/public/model/action-3#deleteFocus"),

    UNLINK("http://midpoint.evolveum.com/xml/ns/public/model/action-3#unlink"),

    DELETE_SHADOW("http://midpoint.evolveum.com/xml/ns/public/model/action-3#deleteShadow"),

    INACTIVATE_SHADOW("http://midpoint.evolveum.com/xml/ns/public/model/action-3#inactivateShadow"),

    INACTIVATE_FOCUS("http://midpoint.evolveum.com/xml/ns/public/model/action-3#inactivateFocus"),

    LINK_ACCOUNT("http://midpoint.evolveum.com/xml/ns/public/model/action-3#linkAccount", true),

    MODIFY_USER("http://midpoint.evolveum.com/xml/ns/public/model/action-3#modifyUser", true),

    SYNCHRONIZE("http://midpoint.evolveum.com/xml/ns/public/model/action-3#synchronize", true),

    ADD_USER("http://midpoint.evolveum.com/xml/ns/public/model/action-3#addUser", true),

    DELETE_USER("http://midpoint.evolveum.com/xml/ns/public/model/action-3#deleteUser", true),

    UNLINK_ACCOUNT("http://midpoint.evolveum.com/xml/ns/public/model/action-3#unlinkAccount", true),

    DELETE_ACCOUNT("http://midpoint.evolveum.com/xml/ns/public/model/action-3#deleteAccount", true),

    DISABLE_ACCOUNT("http://midpoint.evolveum.com/xml/ns/public/model/action-3#disableAccount", true),

    DISABLE_USER("http://midpoint.evolveum.com/xml/ns/public/model/action-3#disableUser", true);

    private final String uri;

    private final boolean deprecated;

    SyncAction(String uri) {
        this(uri, false);
    }

    SyncAction(String uri, boolean deprecated) {
        this.uri = uri;
        this.deprecated = deprecated;
    }

    public String getUri() {
        return uri;
    }

    public boolean isDeprecated() {
        return deprecated;
    }
}
