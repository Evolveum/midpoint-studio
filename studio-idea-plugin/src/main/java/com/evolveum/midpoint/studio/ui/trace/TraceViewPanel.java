package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.Format;
import com.evolveum.midpoint.studio.impl.trace.OpNode;
import com.evolveum.midpoint.studio.impl.trace.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceViewPanel extends JPanel {

    private JXTreeTable main;

    private JBTextArea logs;
    private CheckboxAction logsWrapText;
    private CheckboxAction logsShowSegmentSeparators;

    private JXTreeTable variables;
    private FormatComboboxAction variablesDisplayAs;
    private CheckboxAction variablesWrapText;
    private JBTextArea variablesValue;

    public TraceViewPanel(List<OpNode> data) {
        super(new BorderLayout());

        initLayout(data);
    }

    private void initLayout(List<OpNode> data) {
        JBSplitter horizontal = new OnePixelSplitter(true);
        add(horizontal, BorderLayout.CENTER);

        JComponent main = initMain(data);
        horizontal.setFirstComponent(main);

        JBSplitter vertical = new OnePixelSplitter(false);

        JComponent logs = initLogs();
        vertical.setFirstComponent(logs);

        JComponent variables = initVariables();
        vertical.setSecondComponent(variables);

        horizontal.setSecondComponent(vertical);
    }

    private JComponent initMain(List<OpNode> data) {
        List<TreeTableColumnDefinition> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Operation", 500, o -> o.getOperationNameFormatted()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("State", 60, o -> o.getClockworkState()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("EW", 35, o -> o.getExecutionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("PW", 35, o -> o.getProjectionWave()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Status", 100, o -> o.getResult().getStatus().toString()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("W", 20, o -> o.getImportanceSymbol()));

        long start = System.currentTimeMillis();    // todo fix
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Start", 60, o -> Long.toString(o.getStart(start))));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Time", 80, o -> formatTime(o.getResult().getMicroseconds())));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("Type", 100, o -> o.getType().toString()));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("OH", 50, o -> formatPercent(o.getOverhead())));
        columns.add(new TreeTableColumnDefinition<OpNode, String>("OH2", 50, o -> formatPercent(o.getOverhead2())));

        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY, false, false);
        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY_READ, false, true);
        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY_WRITE, false, true);
        addPerformanceColumn(columns, PerformanceCategory.REPOSITORY_CACHE, true, false);
        addPerformanceColumn(columns, PerformanceCategory.MAPPING_EVALUATION, false, false);
        addPerformanceColumn(columns, PerformanceCategory.ICF, false, false);
        addPerformanceColumn(columns, PerformanceCategory.ICF_READ, false, true);
        addPerformanceColumn(columns, PerformanceCategory.ICF_WRITE, false, true);

        columns.add(new TreeTableColumnDefinition<OpNode, String>("Log", 50, o -> Integer.toString(o.getLogEntriesCount())));

        main = new JXTreeTable(new TraceTreeTableModel(columns, data));
        main.setRootVisible(false);

        for (int i = 0; i < columns.size(); i++) {
            TreeTableColumnDefinition def = columns.get(i);
            main.getColumnModel().getColumn(i).setPreferredWidth(def.getSize());
        }

        main.packAll();

        return new JBScrollPane(main);
    }

    private JComponent initVariables() {
        JBSplitter variables = new OnePixelSplitter(true);

        this.variables = new JXTreeTable(new DefaultTreeTableModel(new DefaultMutableTreeTableNode("aaa")));
        variables.setFirstComponent(new JBScrollPane(this.variables));

        JPanel root = new BorderLayoutPanel();

        DefaultActionGroup group = new DefaultActionGroup();
        variablesDisplayAs = new FormatComboboxAction();
        group.add(variablesDisplayAs);
        variablesWrapText = new SimpleCheckboxAction("Wrap text");
        group.add(variablesWrapText);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewVariablesToolbar", group, true);
        root.add(toolbar.getComponent(), BorderLayout.NORTH);

        variablesValue = new JBTextArea();
        root.add(new JBScrollPane(variablesValue), BorderLayout.CENTER);

        variables.setSecondComponent(root);

        return new HeaderDecorator("Variables", variables);
    }

    private JComponent initLogs() {
        JPanel root = new BorderLayoutPanel();

        DefaultActionGroup group = new DefaultActionGroup();
        logsWrapText = new SimpleCheckboxAction("Wrap text");
        group.add(logsWrapText);
        logsShowSegmentSeparators = new SimpleCheckboxAction("Show segment separators");
        group.add(logsShowSegmentSeparators);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewLogsToolbar", group, true);
        root.add(toolbar.getComponent(), BorderLayout.NORTH);

        logs = new JBTextArea();
        root.add(new JBScrollPane(logs));

        return new HeaderDecorator("Logs", root);
    }

    private void addPerformanceColumn(List<TreeTableColumnDefinition> columns, PerformanceCategory category, boolean hidable, boolean readWrite) {
        columns.add(new TreeTableColumnDefinition<OpNode, String>(category.getShortLabel() + " #", 70, o -> Integer.toString(o.getPerformanceByCategory().get(category).getTotalCount())));

//        countColumn.setLabelProvider(new ColumnLabelProvider() {
//            @Override
//            public String getText(Object element) {
//                return String.valueOf(getCount(element));
//            }
//
//            private int getCount(Object element) {
//                return ((OpNode) element).getPerformanceByCategory().get(category).getTotalCount();
//            }
//
//            @Override
//            public Color getForeground(Object element) {
//                return TracePerformanceView.getColor(getCount(element));
//            }
//        });
//        if (readWrite) {
//            readWriteOpColumns.add(countColumn);
//        } else if (hidable) {
//            hidablePerformanceColumns.add(countColumn);
//        }
//        //countColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getPerformanceByCategory().get(category).getTotalCount())));

        columns.add(new TreeTableColumnDefinition<OpNode, String>(category.getShortLabel() + " time", 80, o -> formatTime(o.getPerformanceByCategory().get(category).getTotalTime())));

//        timeColumn.setLabelProvider(new ColumnLabelProvider() {
//            @Override
//            public String getText(Object element) {
//                return formatTime(getTime(element));
//            }
//
//            private long getTime(Object element) {
//                return ((OpNode) element).getPerformanceByCategory().get(category).getTotalTime();
//            }
//
//            @Override
//            public Color getForeground(Object element) {
//                return TracePerformanceView.getColor(getTime(element));
//            }
//        });
//        if (readWrite) {
//            readWriteOpColumns.add(countColumn);
//        } else {
//            hidablePerformanceColumns.add(timeColumn);
//        }
    }

    private static String formatTime(Long time) {
        if (time == null) {
            return "";
        } else {
            return String.format(Locale.US, "%.1f", time / 1000.0);
        }
    }

    private static String formatPercent(Double value) {
        if (value == null) {
            return "";
        } else {
            return String.format(Locale.US, "%.1f%%", value * 100);
        }
    }

    private static class FormatComboboxAction extends ComboBoxAction {

        private Format format = Format.AUTO;

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            DefaultActionGroup group = new DefaultActionGroup();

            for (Format f : Format.values()) {
                group.add(new AnAction() {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        setFormat(f);
                    }
                });
            }

            return group;
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);

            String text = format.getDisplayName();
            getTemplatePresentation().setText(text);
            e.getPresentation().setText(text);
        }

        public void setFormat(Format format) {
            this.format = format;
        }

        public Format getFormat() {
            return format != null ? format : Format.AUTO;
        }
    }
}
