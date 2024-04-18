package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.impl.query.PagingConvertor;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.prism.query.builder.S_ConditionEntry;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.browse.ComboObjectTypes;
import com.evolveum.midpoint.studio.action.browse.ComboQueryType;
import com.evolveum.midpoint.studio.action.browse.DownloadAction;
import com.evolveum.midpoint.studio.action.task.BackgroundableTask;
import com.evolveum.midpoint.studio.action.transfer.DeleteAction;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.*;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryHints;
import com.evolveum.midpoint.studio.lang.axiomquery.AxiomQueryLanguage;
import com.evolveum.midpoint.studio.ui.browse.ObjectsTreeTable;
import com.evolveum.midpoint.studio.ui.browse.ObjectsTreeTableModel;
import com.evolveum.midpoint.studio.util.MavenUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.PagingType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.xml.namespace.QName;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.evolveum.midpoint.studio.util.MidPointUtils.createAnAction;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BrowseToolPanel extends SimpleToolWindowPanel {

    private static final Logger LOG = Logger.getInstance(BrowseToolPanel.class);

    private static final String NOTIFICATION_KEY = "MidPoint Browser";

    private static final String EMPTY_XML_QUERY =
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

    private EditorTextField query;
    private ObjectsTreeTable results;

    private ComboObjectTypes objectType;
    private ComboQueryType queryType;

    private AnAction searchAction;
    private AnAction cancelAction;
    private AnAction pagingAction;

    private AnAction downloadAction;
    private AnAction showAction;
    private AnAction deleteAction;
    private AnAction processAction;

    private TextAction pagingText;

    private boolean rawSearch = true;
    private boolean rawDownload = true;

    private Paging paging = new Paging();
    private QName nameFilterType = Constants.Q_SUBSTRING;

    private ProcessResultsOptions processResultsOptions = new ProcessResultsOptions();

    public BrowseToolPanel(Project project) {
        super(false, true);

        this.project = project;
        boolean useActivities = MavenUtils.isMidpointVersionGreaterThan(project, "4.4");
        this.processResultsOptions.getOptions().setUseActivities(useActivities);

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
        toolbar.setTargetComponent(this);
        root.add(toolbar.getComponent(), BorderLayout.NORTH);

        query = createQueryTextField(ComboQueryType.Type.NAME_OR_OID);
        root.add(ScrollPaneFactory.createScrollPane(query, true), BorderLayout.CENTER);

        return root;
    }

    private JComponent initResultsPanel() {
        JPanel results = new JPanel(new BorderLayout());

        DefaultActionGroup resultsActions = createResultsActionGroup();

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("BrowseResultsActions",
                resultsActions, true);
        resultsActionsToolbar.setTargetComponent(this);
        results.add(resultsActionsToolbar.getComponent(), BorderLayout.NORTH);

        this.results = new ObjectsTreeTable();
        results.add(new JBScrollPane(this.results), BorderLayout.CENTER);

        return results;
    }

    private EditorTextField createQueryTextField(ComboQueryType.Type type) {
        Language lang;
        switch (type) {
            case QUERY_XML:
                lang = XMLLanguage.INSTANCE;
                break;
            case AXIOM:
                lang = AxiomQueryLanguage.INSTANCE;
                break;
            case OID:
            case NAME:
            case NAME_OR_OID:
            default:
                lang = PlainTextLanguage.INSTANCE;
        }

        EditorTextField editor = EditorTextFieldProvider.getInstance()
                .getEditorField(lang, project, List.of(MonospaceEditorCustomization.getInstance()));
        editor.setOneLineMode(false);

        if (objectType != null && editor.getDocument() != null) {
            QName typeHint = objectType.getSelected() != null ? objectType.getSelected().getTypeQName() : null;
            editor.getDocument().putUserData(AxiomQueryHints.ITEM_TYPE_HINT, typeHint);
        }

        return editor;
    }

    private DefaultActionGroup createQueryActionGroup() {
        DefaultActionGroup group = new DefaultActionGroup();
        objectType = new ComboObjectTypes();
        objectType.addSelectionListener(selected -> {
            if (query != null && query.getDocument() != null) {
                QName typeHint = objectType.getSelected() != null ? objectType.getSelected().getTypeQName() : null;
                query.getDocument().putUserData(AxiomQueryHints.ITEM_TYPE_HINT, typeHint);
            }
        });
        group.add(objectType);

        queryType = new ComboQueryType() {

            @Override
            public void setSelected(Type selected) {
                super.setSelected(selected);

                if (queryType.getSelected() == null || query == null) {
                    return;
                }


                String text = query.getText();
                Container parent = query.getParent();
                parent.remove(query);

                query = createQueryTextField(queryType.getSelected());
                query.setText(text);
                parent.add(query);

                switch (queryType.getSelected()) {
                    case QUERY_XML:
                        if (StringUtils.isEmpty(query.getText())) {
                            query.setText(EMPTY_XML_QUERY);
                        }
                        break;
                    case NAME:
                    case NAME_OR_OID:
                    case OID:
                        // todo file type
                        break;
                    case AXIOM:
                        if (StringUtils.isEmpty(query.getText())) {
                            query.setText("");
                        }
                        break;
                }
            }
        };
        group.add(queryType);

        CheckboxAction rawSearch = new CheckboxAction("Raw") {

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

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

        searchAction = new AsyncAction<>("Search", AllIcons.Actions.Find) {

            @Override
            protected BackgroundableTask createTask(AnActionEvent e, Environment env) {
                return new BackgroundableTask(e.getProject(), "Searching objects", "Searching objects") {

                    @Override
                    protected void doRun(ProgressIndicator indicator) {
                        searchPerformed(e, indicator);
                    }
                };
            }

            @Override
            protected boolean isActionEnabled(AnActionEvent evt) {
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

        AnAction expandAll = MidPointUtils.createAnAction(
                "Expand All",
                AllIcons.Actions.Expandall,
                e -> TreeUtil.expandAll(this.results.getTree()));
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction(
                "Collapse All",
                AllIcons.Actions.Collapseall,
                e -> TreeUtil.collapseAll(this.results.getTree(), -1));
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
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

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

        processAction = createAnAction("Process", AllIcons.Actions.RealIntentionBulb, e -> processPerformed(e));
        group.add(processAction);


        group.add(new Separator());

        pagingText = new TextAction() {

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

            @NotNull
            @Override
            public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
                JComponent comp = super.createCustomComponent(presentation, place);
                comp.setBorder(new CompoundBorder(comp.getBorder(), JBUI.Borders.empty(0, 5)));

                return comp;
            }

            @Override
            protected String createText(AnActionEvent evt) {
                return createPagingText(evt);
            }
        };
        group.add(pagingText);

        return group;
    }

    private String createPagingText(AnActionEvent evt) {
        ObjectsTreeTableModel model = getResultsModel();
        int selected = 0;
        int count = 0;

        if (model != null) {
            selected = model.getSelectedObjects().size();
            count = model.getObjects().size();
        }

        if (count == 0) {
            return "Empty";
        }

        return "Returned " + count + " results. Selected " + selected + " objects";
    }

    private String prepareQuery(ComboQueryType.Type queryType, String text) {
        if (queryType == ComboQueryType.Type.QUERY_XML) {
            return text;
        }

        String filterText = switch (queryType) {
            case OID -> createMidpointQueryFilter(text, true, false);
            case NAME -> createMidpointQueryFilter(text, false, true);
            case NAME_OR_OID -> createMidpointQueryFilter(text, true, true);
            case AXIOM -> text;
            default -> throw new IllegalStateException("Unexpected value: " + queryType);
        };

        SearchFilterType filter = new SearchFilterType();
        filter.setText(filterText);

        QueryType query = new QueryType();
        query.setFilter(filter);

        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        QueryFactory qf = ctx.queryFactory();
        ObjectPaging paging = qf.createPaging(this.paging.getFrom(), this.paging.getPageSize(),
                ctx.path(ObjectType.F_NAME), OrderDirection.ASCENDING);
        PagingType pagingType = PagingConvertor.createPagingType(paging);
        query.setPaging(pagingType);

        try {
            RunnableUtils.PluginClassCallable<String> callable = new RunnableUtils.PluginClassCallable<>() {

                @Override
                public String callWithPluginClassLoader() throws Exception {
                    return ctx.serializerFor(PrismContext.LANG_XML).serializeRealValue(query);
                }
            };

            return callable.call();
        } catch (Exception ex) {
            EnvironmentService em = EnvironmentService.getInstance(project);
            Environment env = em.getSelected();

            handleGenericException(env, "Couldn't serialize query", ex);
        }

        return null;
    }

    private String createMidpointQueryFilter(String text, boolean oid, boolean name) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        List<String> values = Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .toList();

        String oidFilter = "";
        if (oid) {
            List<String> oids = values.stream()
                    .filter(s -> MidPointUtils.UUID_PATTERN.matcher(s).matches())
                    .toList();

            if (oids.size() != values.size()) {
                List<String> incorrectOids = values.stream()
                        .filter(s -> !MidPointUtils.UUID_PATTERN.matcher(s).matches())
                        .toList();
                printIncorrectOidsWarning(incorrectOids);
            }

            if (!oids.isEmpty()) {
                List<String> quotedOids = oids.stream()
                        .map(s -> "\"" + s + "\"")
                        .toList();

                // todo this should not be concatenation but via parameters
                oidFilter = ". inOid (" + StringUtils.join(quotedOids, ", ") + ")";
            }
        }

        String nameFilter = "";
        if (name) {
            // todo this should not be concatenation but via parameters
            // name contains[polyStringNorm] "value" or ...
            List<String> nameFilters = values.stream()
                    .map(s -> "name contains[polyStringNorm] \"" + s + "\"")
                    .toList();
            nameFilter = StringUtils.join(nameFilters, " or ");
        }

        if (StringUtils.isNotEmpty(oidFilter) && StringUtils.isNotEmpty(nameFilter)) {
            return nameFilter + " or " + oidFilter;
        }

        if (StringUtils.isNotEmpty(oidFilter)) {
            return oidFilter;
        }

        return nameFilter;
    }

    private void printIncorrectOidsWarning(List<String> incorrect) {
        MidPointService ms = MidPointService.get(project);

        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment env = em.getSelected();
        ms.printToConsole(
                env, BrowseToolPanel.class, "Items in search filed that are not valid OIDs were filtered out ("
                        + StringUtils.join(incorrect, ", ") + ").");
    }

    private void processPerformed(AnActionEvent evt) {
        String query = this.query.getText();
        ComboQueryType.Type queryType = this.queryType.getSelected();
        ObjectTypes type = this.objectType.getSelected();
        List<ObjectType> selected = getResultsModel().getSelectedObjects();

        String preparedQuery = prepareQuery(queryType, query);

        ProcessResultsDialog dialog = new ProcessResultsDialog(processResultsOptions, preparedQuery, type, selected);
        dialog.show();

        if (dialog.isOK() || dialog.isGenerate()) {
            processResultsOptions = dialog.buildOptions();

            performGenerate(evt, selected, processResultsOptions, !dialog.isGenerate());
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
    }

    private void handleGenericException(Environment env, String message, Exception ex) {
        MidPointUtils.handleGenericException(project, env, BrowseToolPanel.class, NOTIFICATION_KEY, message, ex);
    }

    private boolean isResultSelected() {
        ListSelectionModel model = results.getSelectionModel();
        return model.getSelectedItemsCount() != 0;
    }

    private void downloadPerformed(AnActionEvent evt, boolean showOnly, boolean rawDownload) {
        DownloadAction da = new DownloadAction(getResultsModel().getSelectedOids(), showOnly, rawDownload);
        da.setOpenAfterDownload(true);

        ActionManager.getInstance().tryToExecute(da, evt.getInputEvent(), this, ActionPlaces.UNKNOWN, false);
    }

    private void deletePerformed(AnActionEvent evt, boolean rawDownload) {
        DeleteAction da = new DeleteAction();
        da.setRaw(rawDownload);
        da.setOids(getResultsModel().getSelectedOids());

        ActionManager.getInstance().tryToExecute(da, evt.getInputEvent(), this, ActionPlaces.UNKNOWN, false);
    }

    private void cancelPerformed(AnActionEvent evt) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {

            // todo implement

        });
    }

    private ObjectsTreeTableModel getResultsModel() {
        if (results == null) {
            return null;
        }

        return results.getTableModel();
    }

    private void updateTableModel(SearchResultList result) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ObjectsTreeTableModel tableModel = getResultsModel();
            tableModel.setData(result != null ? result.getList() : null);

            TreeUtil.expandAll(this.results.getTree());
        });
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
            case AXIOM:
                filter = createAxiomFilter(ctx);
        }

        ObjectPaging paging = qf.createPaging(this.paging.getFrom(), this.paging.getPageSize(),
                ctx.path(ObjectType.F_NAME), OrderDirection.ASCENDING);

        return qf.createQuery(filter, paging);
    }

    private ObjectFilter createAxiomFilter(PrismContext ctx) throws SchemaException {
        String text = query.getText();
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        ObjectTypes type = objectType.getSelected();
        return ctx.createQueryParser().parseFilter(type.getClassDefinition(), text);
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
            List<String> filteredOids = filtered;

            if (name) {
                // if search by name or oid is used, filter out items that can't be used in oid filter
                filteredOids = filteredOids.stream()
                        .filter(s -> MidPointUtils.UUID_PATTERN.matcher(s).matches()).collect(Collectors.toList());

                if (filteredOids.size() != filtered.size()) {
                    MidPointService ms = MidPointService.get(project);

                    EnvironmentService em = EnvironmentService.getInstance(project);
                    Environment env = em.getSelected();
                    ms.printToConsole(env, BrowseToolPanel.class,
                            "Items in search filed that are not valid OIDs were filtered out (" + (filtered.size() - filteredOids.size()) + ").");
                }
            }

            if (!filteredOids.isEmpty()) {
                or.addCondition(ctx.queryFor(ObjectType.class)
                        .id(filteredOids.toArray(new String[0]))
                        .buildFilter());
            }
        }

        if (name) {
            PrismPropertyDefinition def = ctx.getSchemaRegistry().findPropertyDefinitionByElementName(ObjectType.F_NAME);
            QName matchingRule = PrismConstants.POLY_STRING_NORM_MATCHING_RULE_NAME;
            List<ObjectFilter> filters = new ArrayList<>();
            for (String s : filtered) {
                S_ConditionEntry builder = ctx.queryFor(ObjectType.class).item(ObjectType.F_NAME);

                ObjectFilter filter = Objects.equals(nameFilterType, Constants.Q_EQUAL_Q) ?
                        builder.eq(s).matching(matchingRule).buildFilter() : builder.contains(s).matching(matchingRule).buildFilter();

                filters.add(filter);
            }
            OrFilter nameOr = qf.createOr(filters);
            or.addCondition(nameOr);
        }

        return or;
    }

    private void performGenerate(AnActionEvent e, List<ObjectType> selected, ProcessResultsOptions options, boolean execute) {
        GeneratorOptions opts = options.getOptions();

        Generator generator = options.getGenerator();
        GeneratorAction ga = new GeneratorAction(generator, opts, selected, execute);

        ActionUtil.invokeAction(ga, this, "BrowseToolPanel", e.getInputEvent(), null);
    }
}
