package com.evolveum.midpoint.studio.impl.lang.xnode.validator;

import com.evolveum.midpoint.prism.xnode.Position;

public record XNodeError(Position position, String msg) {
}
