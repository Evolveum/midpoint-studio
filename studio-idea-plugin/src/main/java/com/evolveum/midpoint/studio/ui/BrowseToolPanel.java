package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SearchResultMetadata;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.action.browse.ComboObjectTypes;
import com.evolveum.midpoint.studio.action.browse.ComboQueryType;
import com.evolveum.midpoint.studio.action.browse.DownloadOptions;
import com.evolveum.midpoint.studio.impl.RestObjectManager;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractRoleType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.xml.namespace.QName;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.evolveum.midpoint.studio.util.MidPointUtils.createAnAction;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BrowseToolPanel extends SimpleToolWindowPanel {

    public enum State {

        DONE, SEARCHING, DOWNLOADING, CANCELING
    }

    private static final String NOTIFICATION_KEY = "MidPoint Browser";

    private Project project;

    private State state = State.DONE;

    private JBTextArea query;
    private JXTreeTable results;

    private ComboObjectTypes objectType;
    private ComboQueryType queryType;

    private AnAction searchAction;
    private AnAction cancelAction;
    private AnAction pagingAction;

    private AnAction downloadAction;
    private AnAction showAction;
    private AnAction processAction;

    private TextAction pagingText;
    private AnAction previous;
    private AnAction next;

    private boolean rawSearch = true;
    private boolean rawDownload = true;

    private Paging paging = new Paging();

    public BrowseToolPanel(Project project) {
        super(false, true);

        this.project = project;

        initLayout();
    }

    private void initLayout() {
        JComponent queryPanel = initQueryPanel();
        JComponent resultsPanel = initResultsPanel();

        OnePixelSplitter split = new OnePixelSplitter(false);
        split.setProportion(0.3f);
        split.setFirstComponent(queryPanel);
        split.setSecondComponent(resultsPanel);

        setContent(split);
    }

    private JComponent initQueryPanel() {
        JPanel root = new JPanel(new BorderLayout());

        DefaultActionGroup group = createQueryActionGroup();

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("BrowseQueryActions",
                group, true);
        root.add(toolbar.getComponent(), BorderLayout.NORTH);

        query = new JBTextArea();
        JBScrollPane pane = new JBScrollPane(query);
        root.add(pane, BorderLayout.CENTER);

        return root;
    }

    private JComponent initResultsPanel() {
        JPanel results = new JPanel(new BorderLayout());

        DefaultActionGroup resultsActions = createResultsActionGroup();

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("BrowseResultsActions",
                resultsActions, true);
        results.add(resultsActionsToolbar.getComponent(), BorderLayout.NORTH);

        List<TreeTableColumnDefinition<ObjectType, ?>> columns = new ArrayList<>();
        columns.add(new TreeTableColumnDefinition<>("Name", 500, o -> MidPointUtils.getOrigFromPolyString(o.getName())));
        columns.add(new TreeTableColumnDefinition<>("Display name", 500, o -> {

            if (!(o instanceof AbstractRoleType)) {
                return null;
            }

            return MidPointUtils.getOrigFromPolyString(((AbstractRoleType) o).getDisplayName());
        }));
        columns.add(new TreeTableColumnDefinition<>("Subtype", 100, o -> StringUtils.join(o.getSubtype(), ", ")));
        columns.add(new TreeTableColumnDefinition<>("Oid", 100, o -> o.getOid()));

        this.results = MidPointUtils.createTable(new BrowseTableModel(columns), (List) columns);
        this.results.setTreeCellRenderer(new NodeRenderer());
        this.results.setOpaque(false);

        this.results.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        results.add(new JBScrollPane(this.results), BorderLayout.CENTER);

        // todo finish nice paging
//        DefaultActionGroup pagingActions = createPagingActionGroup();
//
//        ActionToolbar pagingActionsToolbar = ActionManager.getInstance().createActionToolbar("BrowseResultsPagingActions",
//                pagingActions, true);
//        results.add(pagingActionsToolbar.getComponent(), BorderLayout.SOUTH);

        return results;
    }

    private DefaultActionGroup createPagingActionGroup() {
        DefaultActionGroup group = new DefaultActionGroup();

        // todo create external actions, they should be able to show progress
        previous = createAnAction("Previous", AllIcons.General.ArrowLeft, null, null);
        group.add(previous);

        next = createAnAction("Next", AllIcons.General.ArrowRight, null, null);
        group.add(next);

        group.addSeparator();

        pagingText = new TextAction() {

            @NotNull
            @Override
            public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
                JComponent comp = super.createCustomComponent(presentation, place);
                comp.setBorder(new CompoundBorder(comp.getBorder(), new EmptyBorder(0, 5, 0, 5)));

                return comp;
            }
        };
        group.add(pagingText);

        return group;
    }

    private DefaultActionGroup createQueryActionGroup() {
        DefaultActionGroup group = new DefaultActionGroup();
        objectType = new ComboObjectTypes();
        group.add(objectType);

        queryType = new ComboQueryType();
        group.add(queryType);

        CheckboxAction rawSearch = new CheckboxAction("Raw") {

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(isSearchEnabled());
                super.update(e);
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return BrowseToolPanel.this.rawSearch;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                BrowseToolPanel.this.rawSearch = state;
            }
        };
        group.add(rawSearch);

        pagingAction = createAnAction("Paging", "Paging Settings", AllIcons.General.GearPlain,
                e -> pagingSettingsPerformed(e),
                e -> e.getPresentation().setEnabled(isSearchEnabled()));
        group.add(pagingAction);

        searchAction = new BackgroundAction("Search", AllIcons.Actions.Find, "Searching objects") {

            @Override
            protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
                searchPerformed(evt, indicator);
            }

            @Override
            protected boolean isEnabled() {
                return isSearchEnabled();
            }

            @Override
            protected void onThrowable(@NotNull Throwable error) {
                super.onThrowable(error);
            }

            @Override
            protected void onFinished() {
                state = State.DONE;
            }
        };
        group.add(searchAction);

        cancelAction = createAnAction("Cancel", AllIcons.Actions.Cancel,
                e -> cancelPerformed(e),
                e -> e.getPresentation().setEnabled(isCancelEnabled()));
        group.add(cancelAction);

        return group;
    }

    private DefaultActionGroup createResultsActionGroup() {
        DefaultActionGroup group = new DefaultActionGroup();

        downloadAction = new BackgroundAction("Download", AllIcons.Actions.Download, "Downloading objects") {

            @Override
            protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
                downloadPerformed(evt, false, indicator);
            }

            @Override
            protected boolean isEnabled() {
                return isDownloadShowEnabled();
            }

            @Override
            protected void onThrowable(@NotNull Throwable error) {
                // todo
            }

            @Override
            protected void onFinished() {
                state = State.DONE;
            }
        };
        group.add(downloadAction);

        showAction = new BackgroundAction("Show", AllIcons.Actions.Show, "Downloading objects") {

            @Override
            protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
                downloadPerformed(evt, true, indicator);
            }

            @Override
            protected boolean isEnabled() {
                return isDownloadShowEnabled();
            }

            @Override
            protected void onThrowable(@NotNull Throwable error) {
                // todo
            }

            @Override
            protected void onFinished() {
                state = State.DONE;
            }
        };
        group.add(showAction);

        CheckboxAction rawSearch = new CheckboxAction("Raw") {

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(isDownloadShowEnabled());
                super.update(e);
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return BrowseToolPanel.this.rawDownload;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                BrowseToolPanel.this.rawDownload = state;
            }
        };
        group.add(rawSearch);

        group.add(new Separator());

        processAction = createAnAction("Process", AllIcons.Actions.RealIntentionBulb,
                e -> processPerformed(e),
                e -> e.getPresentation().setEnabled(isDownloadShowEnabled()));
        group.add(processAction);

        return group;
    }

    private void processPerformed(AnActionEvent evt) {
        ProcessResultsDialog dialog = new ProcessResultsDialog();
        if (!dialog.showAndGet()) {
            return;
        }


        // todo implement
    }

    private void searchPerformed(AnActionEvent evt, ProgressIndicator indicator) {
        state = State.SEARCHING;

        RestObjectManager rest = RestObjectManager.getInstance(evt.getProject());

        String envName = rest.getEnvironment().getName();

        indicator.setText("Searching objects in " + envName + " MidPoint");

        ObjectTypes type = objectType.getSelected();
        ObjectQuery query = buildQuery(evt.getProject());

        SearchResultList result = null;
        try {
            result = rest.search(type.getClassDefinition(), query, rawSearch);
        } catch (Exception ex) {
            handleGenericException("Couldn't search objects", ex);
        }

        updateTableModel(result);
        // todo finish nice paging
        // updatePagingAction(result, query.getOffset());
    }

    private void handleGenericException(String message, Exception ex) {
        MidPointUtils.handleGenericException(project, NOTIFICATION_KEY, message, ex);
    }

    private List<String> getSelectedRowsOids() {
        List<String> selected = new ArrayList<>();

        ListSelectionModel selectionModel = results.getSelectionModel();
        int[] indices = selectionModel.getSelectedIndices();

//        results.getSelectionModel().ge

        //todo fix
        return selected;
    }

    private boolean isResultSelected() {
        return results.getSelectionModel().getSelectedItemsCount() != 0;
    }

    private void downloadPerformed(AnActionEvent evt, boolean showOnly, ProgressIndicator indicator) {
        ApplicationManager.getApplication().runWriteAction(() -> {

            setState(BrowseToolPanel.State.DOWNLOADING);

            RestObjectManager rest = RestObjectManager.getInstance(evt.getProject());

            ObjectQuery objectQuery = null;
//            todo fix
//            if (table.getRowCount() == table.getSelectedRowCount() || table.getSelectedRowCount() == 0) {
//                // return all
//                objectQuery = buildQuery(evt.getProject());
//            } else {
//                // return only selected objects
//                List<String> oids = getSelectedRowsOids();
//
//                PrismContext ctx = rest.getPrismContext();
//                QueryFactory qf = ctx.queryFactory();
//
//                InOidFilter inOidFilter = qf.createInOid(oids);
//
//                ItemPath path = ctx.path(ObjectType.F_NAME);
//                ObjectPaging paging = qf.createPaging(this.paging.getFrom(), this.paging.getPageSize(),
//                        path, OrderDirection.ASCENDING);
//
//                objectQuery = qf.createQuery(inOidFilter, paging);
//            }

            ObjectTypes objectTypes = objectType.getSelected();
            VirtualFile[] files = rest.download(objectTypes.getClassDefinition(), objectQuery,
                    new DownloadOptions().showOnly(showOnly).raw(rawDownload));

            if (files != null && files.length == 1) {
                FileEditorManager fem = FileEditorManager.getInstance(evt.getProject());
                fem.openFile(files[0], true, true);
            }
            // todo if files is null show error/warning
        });
    }

    private void cancelPerformed(AnActionEvent evt) {
        state = State.CANCELING;

        ApplicationManager.getApplication().executeOnPooledThread(() -> {

            // todo implement

            state = State.DONE;
        });
    }

    private void updatePagingAction(SearchResultList result, int from) {
        SearchResultMetadata metadata = result.getMetadata();

        int to = from + result.getList().size();
        Integer of = 0;
        if (metadata != null) {
            of = metadata.getApproxNumberOfAllResults();
        }

        int selected = getSelectedRowsOids().size();

        String ofStr = of == null ? "unknown" : Integer.toString(of);

        pagingText.setText("From " + from + " to " + to + " of " + ofStr + ". Selected " + selected + " objects");
    }

    private void updateTableModel(SearchResultList result) {
        BrowseTableModel tableModel = (BrowseTableModel) results.getTreeTableModel();
        tableModel.setData(result.getList());

        ApplicationManager.getApplication().invokeLater(() -> {

            tableModel.fireTableDataChanged();
            results.expandAll();
        });

//        printSuccessMessage("");    // todo add paging/count info to message
    }

    private void pagingSettingsPerformed(AnActionEvent evt) {
        PagingDialog dialog = new PagingDialog(paging);
        if (!dialog.showAndGet()) {
            return;
        }

        this.paging = dialog.getPaging();
    }

    private boolean isDownloadShowEnabled() {
        return state == State.DONE && isResultSelected();
    }

    private boolean isSearchEnabled() {
        return state == State.DONE;
    }

    private boolean isCancelEnabled() {
        return state == State.SEARCHING;
    }

    public ObjectTypes getObjectType() {
        return objectType.getSelected();
    }

    public ObjectQuery getObjectQuery(Project project) {
        return buildQuery(project);
    }

    public void setState(State state) {
        this.state = state;
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
        ObjectPaging paging = qf.createPaging(this.paging.getFrom(), this.paging.getPageSize(), path, OrderDirection.ASCENDING);

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
