package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.util.PrismPrettyPrinter;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.prism.xml.ns._public.types_3.EncryptedDataType;
import com.evolveum.prism.xml.ns._public.types_3.HashedDataType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class TraceUtils {

    private static final Logger LOG = Logger.getInstance(TraceUtils.class);

    public static String prettyPrint(PrismValue prismValue) {
        if (prismValue instanceof PrismPropertyValue) {
            return prettyPrint((PrismPropertyValue<?>) prismValue);
        } else if (prismValue instanceof PrismReferenceValue) {
            return prettyPrint((PrismReferenceValue) prismValue);
        } else if (prismValue instanceof PrismContainerValue) {
            return prettyPrint((PrismContainerValue<?>) prismValue);
        } else if (prismValue == null) {
            return "";
        } else {
            return PrettyPrinter.prettyPrint(prismValue);
        }
    }

    public static String prettyPrint(PrismPropertyValue ppv) {
        // LOG.trace("Pretty printing prism property value");

        Object realValue = ppv.getRealValue();
        if (realValue instanceof RawType) {
            // should always be
            RawType raw = (RawType) realValue;
            return raw.extractString();
        } else if (realValue instanceof ProtectedStringType) {
            StringBuilder sb = new StringBuilder();
            ProtectedStringType ps = (ProtectedStringType) realValue;
            if (ps.getClearValue() != null) {
                sb.append(ps.getClearValue());
            }
            EncryptedDataType enc = ps.getEncryptedDataType();
            if (enc != null) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append("E:").append(print(enc));
            }
            HashedDataType hashed = ps.getHashedDataType();
            if (hashed != null) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append("H:").append(print(hashed));
            }
            return sb.toString();
        } else {
            return PrismPrettyPrinter.prettyPrint(ppv);
        }
    }

    private static String print(HashedDataType hashed) {
        return dumpHex(hashed.getDigestValue(), 5);
    }

    private static String print(EncryptedDataType enc) {
        byte[] value = enc.getCipherData() != null ? enc.getCipherData().getCipherValue() : null;
        return dumpHex(value, 5);
    }

    public static String dumpHex(byte[] value, int MAX) {
        StringBuilder sb = new StringBuilder();
        if (value != null) {
            for (int i = 0; i < Math.min(MAX, value.length); i++) {
                sb.append(String.format("%02x", value[i]));
            }
            if (value.length > MAX) {
                sb.append("...");
            }
        }
        return sb.toString();
    }

    public static String prettyPrint(PrismReferenceValue prv) {
        StringBuilder sb = new StringBuilder();
        if (prv.getTargetType() != null) {
            sb.append(prv.getTargetType().getLocalPart());
            sb.append(": ");
        }
        sb.append(prv.getOid());
        if (prv.getTargetName() != null) {
            sb.append(" (").append(prv.getTargetName()).append(")");
        }
        return sb.toString();
    }

    public static String prettyPrint(PrismContainerValue<?> pcv) {
        if (pcv.getItems().size() > 3) {
            return "(" + pcv.getItems().size() + " items)";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Item<?, ?> item : pcv.getItems()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(item.getElementName().getLocalPart());
                sb.append(": ");
                sb.append(prettyPrint(item));
            }
            return sb.toString();
        }
    }

    public static String prettyPrint(Item<?, ?> item) {
        if (item == null) {
            return "";
        } else if (item.isEmpty()) {
            return "";
        } else if (item.size() == 1) {
            return prettyPrint(item.getValues().get(0));
        } else if (item.size() <= 3) {
            return item.getValues().stream().map(value -> prettyPrint(value)).collect(Collectors.joining("; "));
        } else {
            return "(" + item.size() + " values)";
        }
    }

    public static String prettyPrint(List<PrismValue> values) {
        return values.stream().map(value -> prettyPrint(value)).collect(Collectors.joining("; "));
    }
}
