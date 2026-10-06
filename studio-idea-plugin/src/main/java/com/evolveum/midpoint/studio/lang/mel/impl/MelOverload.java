package com.evolveum.midpoint.studio.lang.mel.impl;

import java.util.List;

/**
 * One CEL overload of a MEL function. For member overloads, parameterTypes includes the
 * receiver as the first element (matching CEL's declaration model).
 */
record MelOverload(boolean member, List<String> parameterTypes, String returnType, String documentation) {
}
