package com.evolveum.midpoint.studio;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointIcons {

    public static final Icon ACTION_MIDPOINT = IconLoader.findIcon("/icons/midpoint.png");

    public static final Icon ACTION_RANDOM_OID = IconLoader.findIcon("/icons/random_oid.png");

    /**
     * @deprecated {@link com.intellij.icons.AllIcons.Actions} BuildLoadChanges, only for backward compatibility with idea 193 and 201
     */
    @Deprecated
    public static final Icon ACTION_BUILD_LOAD_CHANGES = IconLoader.findIcon("/icons/buildLoadChanges.svg");

    /**
     * @deprecated {@link com.intellij.icons.AllIcons.Actions} DeleteTagHover, only for backward compatibility with idea 193 and 201
     */
    @Deprecated
    public static final Icon ACTION_DELETE_TAG_HOVER = IconLoader.findIcon("/icons/deleteTagHover.svg");
}
