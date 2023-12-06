package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;
import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.*;

public class SchemaTypesProvider extends TextFieldWithAutoCompletionListProvider<String> {

    private final SchemaRegistry registry;
    private final DynamicNamespacePrefixMapper prefixMapper;

    public SchemaTypesProvider(@NotNull SchemaRegistry registry) {
        super(new ArrayList<>());

        this.registry = registry;
        this.prefixMapper = registry.getNamespacePrefixMapper();

        init();
    }

    private void init() {
        Set<QName> types = new HashSet<>();

        for (PrismSchema schema : registry.getSchemas()) {
            schema.getComplexTypeDefinitions().forEach(ctd -> types.add(ctd.getTypeName()));
        }

        List<String> result = types.stream()
                .map(this::stringToQName)
                .sorted()
                .toList();

        setItems(result);
    }

    @Override
    protected @NotNull String getLookupString(@NotNull String item) {
        return item;
    }

    public boolean isValid(String item) {
        return getItems(item, false, null).stream()
                .filter(i -> i.equals(item))
                .count() == 1;
    }

    public String stringToQName(QName qname) {
        if (qname == null) {
            return "";
        }

        String prefix = prefixMapper.getPrefix(qname.getNamespaceURI());

        return StringUtils.isEmpty(prefix) ? qname.getLocalPart() : prefix + ":" + qname.getLocalPart();
    }

    public QName qnameToString(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        QNameUtil.PrefixedName name = QNameUtil.parsePrefixedName(value);

        Map<String, String> map = prefixMapper.getNamespacesDeclaredByDefault();

        if (StringUtils.isNotEmpty(name.prefix())) {
            String namespace = map.get(name.prefix());
            return new QName(namespace, name.localName());
        }

        return new QName(registry.getDefaultNamespace(), name.localName());
    }
}
