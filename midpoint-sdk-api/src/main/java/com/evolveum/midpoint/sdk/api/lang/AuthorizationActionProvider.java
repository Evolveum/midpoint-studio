package com.evolveum.midpoint.sdk.api.lang;

import java.util.Set;

public interface AuthorizationActionProvider {

    Set<Action> getActions();
}
