package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.impl.query.EqualFilterImpl;
import com.evolveum.midpoint.prism.impl.query.SubstringFilterImpl;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SearchResultMetadata;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.browse.*;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.*;
import com.evolveum.midpoint.studio.impl.service.MidPointLocalizationService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractRoleType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.evolveum.midpoint.studio.util.MidPointUtils.createAnAction;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BrowseToolPanel extends SimpleToolWindowPanel {

    private static final Logger LOG = Logger.getInstance(BrowseToolPanel.class);

    private static final String NOTIFICATION_KEY = "MidPoint Browser";

    private static final String EMPTY_QUERY =
            "<query xmlns=\"http://prism.evolveum.com/xml/ns/public/query-3\">\n" +
                    "    <filter>\n" +
                    "        <!-- insert filter -->\n" +
                    "    </filter>\n" +
                    "    <paging>\n" +
                    "        <offset>0</offset>\n" +
                    "        <maxSize>500</maxSize>\n" +
                    "    </paging>\n" +
                    "</query>";

    private Project project;

    private JBTextArea query;
    private JXTreeTable results;

    private ComboObjectTypes objectType;
    private ComboQueryType queryType;

    private BackgroundAction searchAction;
    private AnAction cancelAction;
    private AnAction pagingAction;

    private AnAction downloadAction;
    private AnAction showAction;
    private AnAction deleteAction;
    private AnAction processAction;

    private TextAction pagingText;
    private AnAction previous;
    private AnAction next;

    private boolean rawSearch = true;
    private boolean rawDownload = true;

    private Paging paging = new Paging();
    private QName nameFilterType = Constants.Q_SUBSTRING;

    private ProcessResultsOptions processResultsOptions = new ProcessResultsOptions();

    public BrowseToolPanel(Project project) {
        super(false, true);

        this.project = project;

        initLayout();
    }

    private void initLayout() {
        JComponent queryPanel = initQueryPanel();
        JComponent resultsPanel = initResultsPanel();

        OnePixelSplitter split = new OnePixelSplitter(false);
        split.setProportion(0.4f);
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
        columns.add(new TreeTableColumnDefinition<>("Name", 500, null));
        columns.add(new TreeTableColumnDefinition<>("Display name", 500, o -> {

            if (!(o instanceof AbstractRoleType)) {
                return null;
            }

            return MidPointUtils.getOrigFromPolyString(((AbstractRoleType) o).getDisplayName());
        }));
        columns.add(new TreeTableColumnDefinition<>("Subtype", 100, o -> StringUtils.join(o.getSubtype(), ", ")));
        columns.add(new TreeTableColumnDefinition<>("Oid", 100, o -> o.getOid()));

        this.results = MidPointUtils.createTable(new BrowseTableModel(columns), (List) columns);
        this.results.setTreeCellRenderer(new NodeRenderer() {

            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded,
                                              boolean leaf, int row, boolean hasFocus) {
                Object node = TreeUtil.getUserObject(value);
                DefaultMutableTreeTableNode treeNode = (DefaultMutableTreeTableNode) node;

                Object userObject = treeNode.getUserObject();
                if (userObject instanceof ObjectTypes) {
                    ObjectTypes type = (ObjectTypes) userObject;
                    String text = type.getTypeQName().getLocalPart();

                    value = ServiceManager.getService(MidPointLocalizationService.class).translate("ObjectType." + text, text);
                } else if (userObject instanceof ObjectType) {
                    ObjectType ot = (ObjectType) userObject;
                    value = MidPointUtils.getOrigFromPolyString(ot.getName());
                }

                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        });
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
                comp.setBorder(new CompoundBorder(comp.getBorder(), JBUI.Borders.empty(0, 5)));

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

        queryType = new ComboQueryType() {

            @Override
            public void setSelected(Type selected) {
                super.setSelected(selected);

                if (queryType.getSelected() == null || query == null) {
                    return;
                }

                switch (queryType.getSelected()) {
                    case QUERY_XML:
                        if (StringUtils.isEmpty(query.getText())) {
                            query.setText(EMPTY_QUERY);
                        }
                        break;
                    case NAME:
                    case NAME_OR_OID:
                    case OID:
                        // todo file type
                        break;
                }


            }
        };
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

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> results.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> results.collapseAll());
        group.add(collapseAll);

        group.add(new Separator());

        downloadAction = createAnAction("Download", AllIcons.Actions.Download,
                e -> downloadPerformed(e, false, rawDownload),
                e -> e.getPresentation().setEnabled(isDownloadShowEnabled()));
        group.add(downloadAction);

        showAction = createAnAction("Show", AllIcons.Actions.Show,
                e -> downloadPerformed(e, true, rawDownload),
                e -> e.getPresentation().setEnabled(isDownloadShowEnabled()));
        group.add(showAction);

        deleteAction = createAnAction("Delete", AllIcons.Vcs.Remove,
                e -> deletePerformed(e, rawDownload),
                e -> e.getPresentation().setEnabled(isDownloadShowEnabled()));
        group.add(deleteAction);

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
                e -> e.getPresentation().setEnabled(isResultSelected() || StringUtils.isNotEmpty(query.getText() )));
        group.add(processAction);

        return group;
    }

    private void processPerformed(AnActionEvent evt) {
        String query = this.query.getText();
        ComboQueryType.Type queryType = this.queryType.getSelected();
        ObjectTypes type = this.objectType.getSelected();
        List<ObjectType> selected = getResultsModel().getSelectedObjects(results);

        if (ComboQueryType.Type.QUERY_XML != queryType && StringUtils.isNotEmpty(query)) {
            // translate query
            EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
            Environment env = em.getSelected();

            try {
                LOG.debug("Setting up midpoint client");
                MidPointClient client = new MidPointClient(evt.getProject(), env);

                LOG.debug("Translating object query");
                ObjectQuery objectQuery = buildQuery(client);

                PrismContext ctx = client.getPrismContext();
                QueryConverter converter = ctx.getQueryConverter();
                QueryType q = converter.createQueryType(objectQuery);

                SearchFilterType filter = q.getFilter();

                RunnableUtils.PluginClassCallable<String> callable = new RunnableUtils.PluginClassCallable<>() {

                    @Override
                    public String callWithPluginClassLoader() throws Exception {
                        return ctx.serializerFor(PrismContext.LANG_XML).serialize(filter.getFilterClauseAsRootXNode());
                    }
                };

                query = callable.call();
            } catch (Exception ex) {
                handleGenericException(env, "Couldn't serialize query", ex);
            }
        }

        ProcessResultsDialog dialog = new ProcessResultsDialog(processResultsOptions, query, type, selected);
        dialog.show();

        if (dialog.isOK() || dialog.isGenerate()) {
            processResultsOptions = dialog.buildOptions();

            performGenerate(selected, processResultsOptions, !dialog.isGenerate());
        }
    }

    private void searchPerformed(AnActionEvent evt, ProgressIndicator indicator) {
        LOG.debug("Clearing table");
        // clear result table
        updateTableModel(null);

        // load data
        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        Environment env = em.getSelected();

        indicator.setText("Searching objects in environment: " + env.getName());

        SearchResultList result = null;
        try {
            LOG.debug("Setting up midpoint client");
            MidPointClient client = new MidPointClient(evt.getProject(), env);

            ObjectTypes type = objectType.getSelected();
            ObjectQuery query = buildQuery(client);

            LOG.debug("Starting search");
            result = client.list(type.getClassDefinition(), query, rawSearch);
        } catch (Exception ex) {
            handleGenericException(env, "Couldn't search objects", ex);
        }

        LOG.debug("Updating table");

        // update result table
        updateTableModel(result);

        // todo finish nice paging
        // updatePagingAction(result, query.getOffset());
    }

    private void handleGenericException(Environment env, String message, Exception ex) {
        MidPointUtils.handleGenericException(project, env, BrowseToolPanel.class, NOTIFICATION_KEY, message, ex);
    }

    private List<Pair<String, ObjectTypes>> getSelectedOids() {
        List<Pair<String, ObjectTypes>> selected = new ArrayList<>();

        List<ObjectType> objects = getResultsModel().getSelectedObjects(results);
        objects.forEach(o -> selected.add(new Pair<>(o.getOid(), ObjectTypes.getObjectType(o.getClass()))));

        return selected;
    }

    private boolean isResultSelected() {
        ListSelectionModel model = results.getSelectionModel();
        return model.getSelectedItemsCount() != 0;
    }

    private void downloadPerformed(AnActionEvent evt, boolean showOnly, boolean rawDownload) {
        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        Environment env = em.getSelected();

        DownloadAction da = new DownloadAction(env, getResultsModel().getSelectedOids(results), showOnly, rawDownload) {

            @Override
            protected void onFinished() {
                VirtualFile file = null;// todo getDownloadedFile();
                if (file == null) {
                    return;
                }
                FileEditorManager fem = FileEditorManager.getInstance(evt.getProject());
                fem.openFile(file, true, true);
            }
        };
        ActionManager.getInstance().tryToExecute(da, evt.getInputEvent(), this, ActionPlaces.UNKNOWN, false);
    }

    private void deletePerformed(AnActionEvent evt, boolean rawDownload) {
        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        Environment env = em.getSelected();

        DeleteAction da = new DeleteAction(env, getResultsModel().getSelectedOids(results), rawDownload);
        ActionManager.getInstance().tryToExecute(da, evt.getInputEvent(), this, ActionPlaces.UNKNOWN, false);
    }

    private void cancelPerformed(AnActionEvent evt) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {

            // todo implement

        });
    }

    private void updatePagingAction(SearchResultList result, int from) {
        SearchResultMetadata metadata = result.getMetadata();

        int to = from + result.getList().size();
        Integer of = 0;
        if (metadata != null) {
            of = metadata.getApproxNumberOfAllResults();
        }

        int selected = getResultsModel().getSelectedOids(results).size();

        String ofStr = of == null ? "unknown" : Integer.toString(of);

        pagingText.setText("From " + from + " to " + to + " of " + ofStr + ". Selected " + selected + " objects");
    }

    private BrowseTableModel getResultsModel() {
        return (BrowseTableModel) results.getTreeTableModel();
    }

    private void updateTableModel(SearchResultList result) {
        BrowseTableModel tableModel = getResultsModel();
        tableModel.setData(result != null ? result.getList() : null);

        ApplicationManager.getApplication().invokeLater(() -> {

            tableModel.fireTableDataChanged();
            results.expandAll();
        });

//        printSuccessMessage("");    // todo add paging/count info to message
    }

    private void pagingSettingsPerformed(AnActionEvent evt) {
        BrowseSettingsDialog dialog = new BrowseSettingsDialog(nameFilterType, paging);
        if (!dialog.showAndGet()) {
            return;
        }

        this.paging = dialog.getPaging();
        this.nameFilterType = dialog.getNameFilterType();
    }

    private boolean isDownloadShowEnabled() {
        EnvironmentService em = EnvironmentService.getInstance(project);

        return isResultSelected() && em.isEnvironmentSelected();
    }

    private boolean isSearchEnabled() {
        // todo add condition that we're not currently searching
        return EnvironmentService.getInstance(project).isEnvironmentSelected();
    }

    private boolean isCancelEnabled() {
        // todo enable when search is in progress
        return false;
    }

    public ObjectQuery buildQuery(MidPointClient client) throws SchemaException, IOException {
        PrismContext ctx = client.getPrismContext();
        QueryFactory qf = ctx.queryFactory();

        ComboQueryType.Type queryType = this.queryType.getSelected();
        if (ComboQueryType.Type.QUERY_XML == queryType) {
            return parseQuery(client);
        }

        ObjectFilter filter = null;

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
        }

        ObjectPaging paging = qf.createPaging(this.paging.getFrom(), this.paging.getPageSize(),
                ctx.path(ObjectType.F_NAME), OrderDirection.ASCENDING);

        return qf.createQuery(filter, paging);
    }

    private ObjectQuery parseQuery(MidPointClient client) throws SchemaException, IOException {
        String text = query.getText();
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        ObjectTypes type = objectType.getSelected();

        PrismParser parser = client.createParser(text);
        QueryType queryType = parser.parseRealValue(QueryType.class);

        return client.getPrismContext().getQueryConverter().createObjectQuery(type.getClassDefinition(), queryType);
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
            QName matchingRule = PrismConstants.POLY_STRING_NORM_MATCHING_RULE_NAME;
            List<ObjectFilter> filters = new ArrayList<>();
            for (String s : filtered) {
                ObjectFilter filter = Objects.equals(nameFilterType, Constants.Q_EQUAL_Q) ?
                        EqualFilterImpl.createEqual(ctx.path(ObjectType.F_NAME), def, matchingRule, ctx, s) :
                        SubstringFilterImpl.createSubstring(ctx.path(ObjectType.F_NAME), def, ctx, matchingRule, s, false, false);

                filters.add(filter);
            }
            OrFilter nameOr = qf.createOr(filters);
            or.addCondition(nameOr);
        }

        return or;
    }

    private void performGenerate(List<ObjectType> selected, ProcessResultsOptions options, boolean execute) {
        GeneratorOptions opts = options.getOptions();

        if (opts.isBatchByOids() && selected.isEmpty()) {
            return;
        }

        if (opts.isBatchUsingOriginalQuery() && StringUtils.isEmpty(opts.getOriginalQuery())) {
            return;
        }

        Generator generator = options.getGenerator();
        GeneratorAction ga = new GeneratorAction(generator, opts, selected, execute);

        AWTEvent evt = EventQueue.getCurrentEvent();
        InputEvent ie;
        if (evt instanceof InputEvent) {
            ie = (InputEvent) evt;
        } else {
            ie = new MouseEvent(this, ActionEvent.ACTION_PERFORMED, System.currentTimeMillis(), 0, 0, 0, 0, false, 0);
        }

        ActionManager.getInstance().tryToExecute(ga, ie, this, ActionPlaces.UNKNOWN, false);
    }
}
