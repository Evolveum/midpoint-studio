package com.evolveum.midpoint.studio.lang.axiom;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomTokenType extends IElementType {

    public AxiomTokenType(@NotNull @NonNls String debugName) {
        super(debugName, AxiomLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "AxiomTokenType." + super.toString();
    }
}
