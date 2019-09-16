package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.project.Project;
import com.evolveum.midpoint.studio.action.browse.ComboQueryType;
import com.evolveum.midpoint.studio.impl.RestObjectManager;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.xml.namespace.QName;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class QueryPanel extends JPanel {

    private ComboQueryType queryType;

    private JPanel panel;
    private JTextArea query;
    private JTextField offset;
    private JTextField maxSize;

    public QueryPanel(ComboQueryType queryType) {
        super(new BorderLayout());

        this.queryType = queryType;
    }

    private void createUIComponents() {
        panel = new JPanel();
        add(panel, BorderLayout.CENTER);

        offset = new JTextField();
        maxSize = new JTextField();
    }

    public ObjectQuery buildQuery(Project project) {
        RestObjectManager em = RestObjectManager.getInstance(project);
        PrismContext ctx = em.getPrismContext();
        QueryFactory qf = ctx.queryFactory();

        ObjectFilter filter = null;
        ComboQueryType.Type queryType = this.queryType.getSelected();
        switch (queryType) {
            case OID:
                filter = createFilter(ctx, true, false);
                break;
            case NAME:
                filter = createFilter(ctx, false, true);
                break;
            case NAME_OR_OID:
                filter = createFilter(ctx, true, true);
                break;
            case QUERY_XML:
                filter = parseFilter(ctx);
                break;
            case QUERY_SIMPLE:
                filter = parseSimpleFilter(ctx);
        }

        ItemPath path = ctx.path(ObjectType.F_NAME);
        ObjectPaging paging = qf.createPaging(getOffset(), getMaxSize(), path, OrderDirection.ASCENDING);

        return qf.createQuery(filter, paging);
    }

    private ObjectFilter parseSimpleFilter(PrismContext ctx) {
        return null;
    }

    private ObjectFilter parseFilter(PrismContext ctx) {
        String text = query.getText();
        if (StringUtils.isEmpty(text)) {
            return null;
        }

//        try {
//            Unmarshaller unmarshaller = MidPointClientUtils.createUnmarshaller();
//            Object obj = unmarshaller.unmarshal(new ByteArrayInputStream(text.getBytes()));
//            if (obj instanceof JAXBElement) {
//                obj = ((JAXBElement) obj).getValue();
//            }
//
//            if (obj instanceof SearchFilterType) {
//                return (SearchFilterType) obj;
//            }
//
//            throw new IllegalStateException("Unknown type '" + obj.getClass().getName() + "'");
//        } catch (Exception ex) {
//            // todo error handling
//            throw new RuntimeException(ex);
//        }

        return null;
    }

    public int getOffset() {
        return parseInt(offset.getText());
    }

    public int getMaxSize() {
        return parseInt(maxSize.getText());
    }

    private int parseInt(String text) {
        if (text == null || !text.matches("[0-9]+")) {
            return 0;
        }

        return Integer.parseInt(text);
    }

    private ObjectFilter createFilter(PrismContext ctx, boolean oid, boolean name) {
        String text = query.getText();
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        List<String> filtered = new ArrayList<>();

        String[] items = text.split("\n");
        for (String item : items) {
            item = item.trim();
            if (StringUtils.isEmpty(item)) {
                continue;
            }

            filtered.add(item);
        }

        if (filtered.isEmpty()) {
            return null;
        }

        QueryFactory qf = ctx.queryFactory();
        OrFilter or = qf.createOr();

        if (oid) {
            InOidFilter inOid = qf.createInOid(filtered);
            or.addCondition(inOid);
        }

        if (name) {
            PrismPropertyDefinition def = ctx.getSchemaRegistry().findPropertyDefinitionByElementName(ObjectType.F_NAME);
            QName matchingRule = PrismConstants.POLY_STRING_ORIG_MATCHING_RULE_NAME;
            List<ObjectFilter> equals = new ArrayList<>();
            for (String s : filtered) {
                equals.add(qf.createEqual(ctx.path(ObjectType.F_NAME), def, matchingRule, ctx, s));
            }
            OrFilter nameOr = qf.createOr(equals);
            or.addCondition(nameOr);
        }

        return or;
    }
}
