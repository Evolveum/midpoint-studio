package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.util.PrismPrettyPrinter;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.prism.xml.ns._public.types_3.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TraceUtils {

    private static final Logger LOG = Logger.getInstance(TraceUtils.class);

    public static String prettyPrint(Object object) {
        if (object instanceof ItemDeltaItemType) {
            return prettyPrint((ItemDeltaItemType) object);
        } else if (object instanceof ItemDeltaType) {
            return prettyPrint((ItemDeltaType) object);
        } else if (object instanceof DeltaSetTripleType) {
            return prettyPrint((DeltaSetTripleType) object);
        } else if (object instanceof Collection) {
            return prettyPrintCollection((Collection<?>) object);
        } else if (object instanceof RawType) {
            return ((RawType) object).extractString(); // TODO metadata
        } else if (object instanceof PrismValue) {
            return prettyPrint((PrismValue) object);
        } else if (object instanceof Item) {
            return prettyPrint((Item) object);
        } else {
            return String.valueOf(object);
        }
    }

    public static String prettyPrint(ItemDeltaType itemDelta) {
        return prettyPrint(itemDelta, true);
    }

    public static String prettyPrint(ItemDeltaType itemDelta, boolean showPath) {
        if (itemDelta != null) {
            return (showPath ? itemDelta.getPath() + " " : "") +
                    itemDelta.getModificationType() + " " + prettyPrintCollection(itemDelta.getValue());
        } else {
            return "";
        }
    }

    public static String prettyPrint(ItemDeltaItemType itemDeltaItem) {
        if (itemDeltaItem != null) {
            return "Old: " + prettyPrint(itemDeltaItem.getOldItem()) + ", Delta: " + prettyPrint(itemDeltaItem.getDelta());
        } else {
            return "";
        }
    }

    public static String prettyPrint(DeltaSetTripleType triple) {
        StringBuilder sb = new StringBuilder();
        if (triple != null) {
            List<String> components = new ArrayList<>();
            addSet(components, "Plus", triple.getPlus());
            addSet(components, "Minus", triple.getMinus());
            addSet(components, "Zero", triple.getZero());
            sb.append(String.join("; ", components));
        }
        return sb.toString();
    }

    private static void addSet(List<String> components, String label, List<Object> objects) {
        if (!objects.isEmpty()) {
            components.add(label + ": " + prettyPrintCollection(objects));
        }
    }

    public static String prettyPrintCollection(Collection<?> objects) {
        List<String> pretty = new ArrayList<>();
        for (Object object : objects) {
            pretty.add(prettyPrint(object));
        }
        return String.join(", ", pretty);
    }

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

//    public static String prettyPrint(List<PrismValue> values) {
//        return values.stream().map(value -> prettyPrint(value)).collect(Collectors.joining("; "));
//    }

    public static boolean shouldBeVisible(Project project) {
        return true;
        // todo finish - window factories - tool window visibility should be checked not only on startup

//        FileEditorManager fem = FileEditorManager.getInstance(project);
//
//        for (FileEditor editor : fem.getAllEditors()) {
//            if (editor instanceof TraceViewEditor) {
//                return true;
//            }
//        }
//
//        return false;
    }
}
