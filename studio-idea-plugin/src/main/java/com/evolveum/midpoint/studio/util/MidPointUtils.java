package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.common.LocalizationService;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.client.ClientException;
import com.evolveum.midpoint.studio.impl.client.LocalizationServiceImpl;
import com.evolveum.midpoint.studio.impl.client.ServiceFactory;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.facet.FacetManager;
import com.intellij.ide.DataManager;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManagerEx;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.DisposeAwareRunnable;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.xml.namespace.QName;
import java.awt.Color;
import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointUtils {

    public static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static final Comparator<ObjectTypes> OBJECT_TYPES_COMPARATOR = (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getTypeQName().getLocalPart(), o2.getTypeQName().getLocalPart());

    public static final Comparator<ObjectType> OBJECT_TYPE_COMPARATOR = (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(getOrigFromPolyString(o1.getName()), getOrigFromPolyString(o2.getName()));

    public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    private static final Logger LOG = Logger.getInstance(MidPointUtils.class);

    private static final Random RANDOM = new Random();

    public static final PrismContext DEFAULT_PRISM_CONTEXT = ServiceFactory.DEFAULT_PRISM_CONTEXT;

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("\\$(t|T|s|e|n|o)");

    private static final String[] NAMES;

    static {
        List<String> names = new ArrayList<>();
        for (ObjectTypes t : ObjectTypes.values()) {
            if (t.getClassDefinition() == null || Modifier.isAbstract(t.getClassDefinition().getModifiers())) {
                continue;
            }

            names.add(t.getElementName().getLocalPart());
        }

        NAMES = names.toArray(new String[names.size()]);
    }

    @Deprecated
    public static Project getCurrentProject() {
        DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
        return DataKeys.PROJECT.getData(dataContext);
    }

    public static Color generateAwtColor() {
        float hue = RANDOM.nextFloat();
        // Saturation between 0.1 and 0.3
        float saturation = (RANDOM.nextInt(2000) + 1000) / 10000f;

        return Color.getHSBColor(hue, saturation, 0.9f);
    }

    public static LookupElement buildLookupElement(String name, String oid, String source, int priority) {
        LookupElementBuilder builder = buildLookupElement(name, oid, source);
        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, priority);
    }

    public static LookupElementBuilder buildLookupElement(String name, String oid, String source) {
        return LookupElementBuilder.create(oid)
                .withTailText("(" + name + ")")
                .withLookupString(name)
                .withTypeText(source)
                .withBoldness(true)
                .withCaseSensitivity(false);
    }

    public static String escapeObjectName(String name) {
        if (name == null) {
            return null;
        }

        String escaped = StringUtils.stripAccents(name);
        escaped = escaped.replaceAll("[\\s]+", "-");
        return escaped.replaceAll("[^A-Za-z0-9]", "");
    }

    public static void runWhenInitialized(final Project project, final Runnable r) {
        if (project.isDisposed()) {
            return;
        }

        if (isNoBackgroundMode()) {
            r.run();
            return;
        }

        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(DisposeAwareRunnable.create(r, project));
            return;
        }

        runDumbAware(project, r);
    }

    public static boolean isNoBackgroundMode() {
        return ApplicationManager.getApplication().isUnitTestMode()
                || ApplicationManager.getApplication().isHeadlessEnvironment();
    }

    public static void runDumbAware(Project project, Runnable runnable) {
        if (DumbService.isDumbAware(runnable)) {
            runnable.run();
        } else {
            DumbService.getInstance(project).runWhenSmart(DisposeAwareRunnable.create(runnable, project));
        }
    }

    public static String replaceFilePath(String text, Map<String, String> properties) {
        Matcher matcher = FILE_PATH_PATTERN.matcher(text);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = properties.get(matcher.group(1));
            if (replacement == null) {
                replacement = "null";
            }
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static Credentials getCredentials(String key) {
        CredentialAttributes attributes = createCredentialAttributes(key);

        PasswordSafe safe = PasswordSafe.getInstance();

        return safe.get(attributes);
    }

    public static String getPassword(String key) {
        CredentialAttributes attributes = createCredentialAttributes(key);

        PasswordSafe safe = PasswordSafe.getInstance();
        return safe.getPassword(attributes);
    }

    public static void setCredentials(String key, String username, String password) {
        CredentialAttributes attributes = createCredentialAttributes(key);
        Credentials credentials = username == null && password == null ? null : new Credentials(username, password);
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    public static void setPassword(String key, String password) {
        CredentialAttributes attributes = createCredentialAttributes(key);
        PasswordSafe.getInstance().setPassword(attributes, password);
    }

    private static CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt
                .generateServiceName(MidPointSettings.class.getSimpleName(), key));
    }

    public static void publishException(Project project, Environment env, Class clazz, String notificationKey, String msg, Exception ex) {
        MidPointService mm = MidPointService.getInstance(project);
        mm.printToConsole(env, clazz, msg + ". Reason: " + ex.getMessage());

        publishExceptionNotification(env, clazz, notificationKey, msg, ex);
    }

    public static void publishExceptionNotification(Environment env, Class clazz, String key, String message, Exception ex) {
        publishExceptionNotification(env, clazz, key, message, ex, new NotificationAction[]{});
    }

    public static void publishExceptionNotification(Environment env, Class clazz, String key, String message, Exception ex, NotificationAction... actions) {
        String msg = message + ", reason: " + ex.getMessage();

        List<NotificationAction> list = new ArrayList<>();
        if (ex instanceof ClientException) {
            ClientException cex = (ClientException) ex;
            OperationResult result = cex.getResult();
            if (result != null) {
                list.add(new ShowResultNotificationAction(result));
            }
        } else {
            list.add(new ShowExceptionNotificationAction("Exception occurred", ex, clazz, env));
        }

        list.addAll(Arrays.asList(actions));

        MidPointUtils.publishNotification(key, "Error", msg, NotificationType.ERROR, list.toArray(new NotificationAction[list.size()]));

        if (LOG.isTraceEnabled()) {
            LOG.trace(msg);
            LOG.trace(ex);
        } else {
            LOG.debug(msg);
        }
    }

    public static void publishNotification(String key, String title, String content, NotificationType type) {
        publishNotification(key, title, content, type, (NotificationAction[]) null);
    }

    public static void publishNotification(String key, String title, String content, NotificationType type,
                                           NotificationAction... actions) {
        Notification notification = new Notification(key, title, content, type);
        if (actions != null) {
            Arrays.stream(actions).filter(a -> a != null).forEach(a -> notification.addAction(a));
        }

        Notifications.Bus.notify(notification);
    }

    public static void handleGenericException(Project project, Environment env, Class clazz, String key, String message, Exception ex) {
        NotificationAction action = null;
        if (ex instanceof ClientException) {
            OperationResult result = ((ClientException) ex).getResult();
            if (result != null) {
                action = new ShowResultNotificationAction(result);
            }
        }

        if (key != null) {
            MidPointUtils.publishNotification(key, "Error",
                    message + ", reason: " + ex.getMessage(), NotificationType.ERROR, action);
        }

        if (project != null) {
            MidPointService manager = MidPointService.getInstance(project);
            manager.printToConsole(env, clazz, message, ex);
        }
    }

    public static Map<String, Object> mapOf(Entry<String, Object>... entries) {
        Map<String, Object> map = new HashMap<>();

        if (entries == null) {
            return map;
        }

        for (Entry<String, Object> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    public static AnAction createAnAction(String text, Icon icon, Consumer<AnActionEvent> actionPerformed) {
        return createAnAction(text, icon, actionPerformed, null);
    }

    public static AnAction createAnAction(String text, Icon icon, Consumer<AnActionEvent> actionPerformed, Consumer<AnActionEvent> update) {
        return createAnAction(text, text, icon, actionPerformed, update);
    }

    public static AnAction createAnAction(String text, String description, Icon icon, Consumer<AnActionEvent> actionPerformed, Consumer<AnActionEvent> update) {
        return new AnAction(text, description, icon) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (actionPerformed != null) {
                    actionPerformed.accept(e);
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                if (update != null) {
                    update.accept(e);
                    return;
                }
                super.update(e);
            }
        };
    }

    public static <R> JXTreeTable createTable(TreeTableModel model, List<TreeTableColumnDefinition<R, ?>> columns) {
        JXTreeTable table = new JXTreeTable();
        table.setRootVisible(false);
        table.setEditable(false);
        table.setDragEnabled(false);
        table.setHorizontalScrollEnabled(true);
        table.setTreeTableModel(model);
        table.setLeafIcon(null);
        table.setClosedIcon(null);
        table.setOpenIcon(null);

        if (columns != null) {
            applyColumnDefinitions(columns, table);
        }

        table.packAll();

        return table;
    }

    @Experimental
    public static <R> JXTreeTable createTable2(TreeTableModel model, TableColumnModelExt columnModel, boolean disableHack) {

        JXTreeTable table = new JXTreeTable(model) {
            @Override
            protected void resetDefaultTableCellRendererColors(Component renderer, int row, int column) {
                if (!disableHack) {
                    super.resetDefaultTableCellRendererColors(renderer, row, column);
                }
            }
        };
        table.setAutoCreateColumnsFromModel(false);
        table.setColumnModel(columnModel);
        table.setRootVisible(false);
        table.setEditable(false);
        table.setDragEnabled(false);
        table.setHorizontalScrollEnabled(true);
        table.setSelectionModel(table.getSelectionModel());        // todo fix
        table.setLeafIcon(null);
        table.setClosedIcon(null);
        table.setOpenIcon(null);

        table.packAll();

        return table;
    }

    private static <R> void applyColumnDefinitions(List<TreeTableColumnDefinition<R, ?>> columnDefinitions, JXTreeTable table) {
        for (int i = 0; i < columnDefinitions.size(); i++) {
            TreeTableColumnDefinition<R, ?> columnDef = columnDefinitions.get(i);

            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnDef.getSize());
            if (columnDef.getTableCellRenderer() != null) {
                column.setCellRenderer(columnDef.getTableCellRenderer());
            }
        }
    }

    public static String getName(PrismObject obj) {
        return obj != null ? getOrigFromPolyString(obj.getName()) : null;
    }

    public static String getOrigFromPolyString(PolyString poly) {
        return poly != null ? poly.getOrig() : null;
    }

    public static String getOrigFromPolyString(PolyStringType poly) {
        return poly != null ? poly.getOrig() : null;
    }

    public static String generateTaskIdentifier() {
        return System.currentTimeMillis() + ":" + Math.round(Math.random() * 1000000000.0);
    }

    public static ObjectTypes commonSuperType(ObjectTypes o1, ObjectTypes o2) {
        if (o1 == null || o2 == null) {
            return null;
        }

        Class<? extends ObjectType> c1 = o1.getClassDefinition();
        Class<? extends ObjectType> c2 = o2.getClassDefinition();

        Class<? extends ObjectType> s = c1;
        while (!s.isAssignableFrom(c2)) {
            s = (Class<? extends ObjectType>) s.getSuperclass();
        }

        return ObjectTypes.getObjectType(s);
    }

    public static boolean isAssignableFrom(ObjectTypes o1, ObjectTypes o2) {
        if (o1 == null || o2 == null) {
            return false;
        }

        return o1.getClassDefinition().isAssignableFrom(o2.getClassDefinition());
    }

    public static QName getTypeQName(ObjectType obj) {
        if (obj == null) {
            return null;
        }

        return getTypeQName(obj.asPrismObject());
    }

    public static QName getTypeQName(PrismObject obj) {
        if (obj == null) {
            return null;
        }

        return ObjectTypes.getObjectType(obj.getCompileTimeClass()).getTypeQName();
    }

    public static String formatTime(Long time) {
        if (time == null) {
            return "";
        } else {
            return String.format(Locale.US, "%.1f", time / 1000.0);
        }
    }

    public static String formatPercent(Double value) {
        if (value == null) {
            return "";
        } else {
            return String.format(Locale.US, "%.1f%%", value * 100);
        }
    }

    public static XmlTag[] findSubTags(XmlTag tag, QName name) {
        if (tag == null) {
            return new XmlTag[0];
        }

        return tag.findSubTags(name.getLocalPart(), name.getNamespaceURI());
    }

    public static XmlTag findSubTag(XmlTag tag, QName name) {
        XmlTag[] tags = findSubTags(tag, name);
        if (tags != null && tags.length > 0) {
            return tags[0];
        }

        return null;
    }

    public static JPanel createBorderLayoutPanel(JComponent north, JComponent center, JComponent south) {
        JPanel panel = new BorderLayoutPanel();
        if (north != null) {
            panel.add(north, BorderLayout.NORTH);
        }

        if (center != null) {
            panel.add(center, BorderLayout.CENTER);
        }

        if (south != null) {
            panel.add(south, BorderLayout.SOUTH);
        }

        return panel;
    }

//    public static void applyTestResult(VirtualFile file, ExecuteActionServerResponse lastResponse) {
//        if (file == null) {
//            return;
//        }
//        if (lastResponse.isSuccess()) {
//            return;
//        }
//
//        try {
//            file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
//            IMarker m = file.createMarker(IMarker.PROBLEM);
//            m.setAttribute(IMarker.LINE_NUMBER, 1);
//            m.setAttribute(IMarker.MESSAGE, "Test resource failed: " + lastResponse.getErrorDescription());
//            m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
//            m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
//        } catch (CoreException e) {
//            Console.logError("Couldn't show validation result: " + e.getMessage(), e);
//        }
//    }
//
//    public static void applyValidationResult(VirtualFile file, String dataOutput) {
//        if (file == null) {
//            return;
//        }
//
//        Element root = DOMUtil.parseDocument(dataOutput).getDocumentElement();
//        Element item = DOMUtil.getChildElement(root, "item");
//        if (item == null) {
//            return;
//        }
//        Element validationResult = DOMUtil.getChildElement(item, "validationResult");
//        if (validationResult == null) {
//            return;
//        }
//        List<Element> issues = DOMUtil.getChildElements(validationResult, new QName(Constants.COMMON_NS, "issue"));
//
//        try {
//            file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
//            for (Element issue : issues) {
//                String severity = getElementText(issue, "severity");
//                String category = getElementText(issue, "category");
//                String code = getElementText(issue, "code");
//                String text = getElementText(issue, "text");
//                String itemPath = getElementText(issue, "itemPath");
//                int severityCode;
//                switch (severity) {
//                    case "error": severityCode = IMarker.SEVERITY_ERROR; break;
//                    case "warning": severityCode = IMarker.SEVERITY_WARNING; break;
//                    default: severityCode = IMarker.SEVERITY_INFO; break;
//                }
//                IMarker m = file.createMarker(IMarker.PROBLEM);
//                m.setAttribute(IMarker.LINE_NUMBER, 1);
//                m.setAttribute(IMarker.MESSAGE, text);
//                m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
//                m.setAttribute(IMarker.SEVERITY, severityCode);
//                m.setAttribute(IMarker.LOCATION, itemPath);
//            }
//
//        } catch (CoreException e) {
//            Console.logError("Couldn't show validation result: " + e.getMessage(), e);
//        }
//    }
//
//    private static String getElementText(Element element, String name) {
//        Element child = DOMUtil.getChildElement(element, name);
//        return child != null ? child.getTextContent() : null;
//    }

    public static QName createQName(XmlTag element) {
        if (element == null) {
            return null;
        }

        return new QName(element.getNamespace(), element.getLocalName());

    }

    public static QName elementXsiType(XmlTag tag) {
        if (tag == null) {
            return null;
        }

        XmlAttribute xsiType = tag.getAttribute("type", NS_XSI);
        if (xsiType == null || xsiType.getValue() == null) {
            return null;
        }

        String namespace;
        String localPart;

        String type = xsiType.getValue();
        String[] array = type.split(":", -1);
        if (array.length == 1) {
            namespace = tag.getNamespace();
            localPart = array[0];
        } else {
            namespace = tag.getNamespaceByPrefix(array[0]);
            localPart = array[1];
        }

        return new QName(namespace, localPart);
    }

    public static <R> TableColumnModelExt createTableColumnModel(List<TreeTableColumnDefinition<R, ?>> columnDefinitions) {
        TableColumnModelExt model = new DefaultTableColumnModelExt();
        int index = 0;
        for (TreeTableColumnDefinition<R, ?> columnDefinition : columnDefinitions) {
            TableColumnExt column = new TableColumnExt(index++, columnDefinition.getSize(),
                    columnDefinition.getTableCellRenderer(), null);
            column.setIdentifier(columnDefinition.getHeader());
            column.setEditable(false);
            column.setTitle(columnDefinition.getHeader());
            model.addColumn(column);
        }
        return model;
    }

    public static boolean isObjectTypeElement(XmlTag tag) {
        if (tag == null) {
            return false;
        }

        QName name = new QName(tag.getNamespace(), tag.getLocalName());
        for (ObjectTypes type : ObjectTypes.values()) {
            if (name.equals(type.getElementName())) {
                return true;
            }
        }

        return false;
    }

    public static List<VirtualFile> filterXmlFiles(VirtualFile[] files) {
        List<VirtualFile> result = new ArrayList<>();
        if (files == null) {
            return result;
        }

        for (VirtualFile selected : files) {
            if (selected.isDirectory()) {
                VfsUtilCore.iterateChildrenRecursively(
                        selected,
                        file -> file.isDirectory() || XmlFileType.DEFAULT_EXTENSION.equalsIgnoreCase(file.getExtension()),
                        file -> {
                            if (!file.isDirectory() && !result.contains(file)) {
                                result.add(file);
                            }
                            return true;
                        });
            } else if (XmlFileType.DEFAULT_EXTENSION.equalsIgnoreCase(selected.getExtension())) {
                if (!result.contains(selected)) {
                    result.add(selected);
                }
            }
        }

        return result;
    }

    public static boolean hasMidPointFacet(@NotNull Project project) {
        ModuleManager mm = ModuleManager.getInstance(project);
        Module[] modules = mm.getModules();
        if (modules == null || modules.length == 0) {
            return false;
        }

        for (Module module : modules) {
            FacetManager fm = FacetManager.getInstance(module);
            if (fm.getFacetByType(MidPointFacetType.FACET_TYPE_ID) != null) {
                return true;
            }
        }

        return false;
    }

    public static boolean isItObjectTypeOidAttribute(PsiElement element) {
        return psiElement().inside(
                XmlPatterns
                        .xmlAttributeValue()
                        .withParent(
                                XmlPatterns.xmlAttribute("oid").withParent(
                                        XmlPatterns.xmlTag().withNamespace(SchemaConstantsGenerated.NS_COMMON)
                                                .withName(NAMES)))).accepts(element);
    }

    public static JBScrollPane borderlessScrollPane(@NotNull JComponent component) {
        JBScrollPane pane = new JBScrollPane(component);
        pane.setBorder(null);

        return pane;
    }

    public static FileEditor[] openFile(Project project, VirtualFile file) {
        if (file == null) {
            return FileEditor.EMPTY_ARRAY;
        }

        FileEditorManager fem = FileEditorManager.getInstance(project);
        return fem.openFile(file, true, true);
    }

    public static boolean isVisibleWithMidPointFacet(AnActionEvent evt) {
        if (evt.getProject() == null) {
            return false;
        }

        boolean hasFacet = MidPointUtils.hasMidPointFacet(evt.getProject());
        evt.getPresentation().setVisible(hasFacet);

        return hasFacet;
    }

    public static boolean shouldEnableAction(AnActionEvent evt) {
        if (!isVisibleWithMidPointFacet(evt)) {
            return false;
        }

        return isEnvironmentAndFileSelected(evt);
    }

    public static boolean isMidpointObjectFileSelected(AnActionEvent evt) {
        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        return toProcess.size() > 0;
    }

    public static boolean isEnvironmentAndFileSelected(AnActionEvent evt) {
        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());

        return em.getSelected() != null && isMidpointObjectFileSelected(evt);
    }

    public static Module guessMidpointModule(Project project) {
        ModuleManager mm = ModuleManager.getInstance(project);
        Module[] modules = mm.getModules();

        if (modules == null || modules.length == 0) {
            return null;
        }

        for (Module module : modules) {
            ModuleRootManagerEx mrm = ModuleRootManagerEx.getInstanceEx(module);
            for (VirtualFile file : mrm.getContentRoots()) {
                if (!file.isDirectory()) {
                    continue;
                }

                VirtualFile objects = file.findChild("objects");
                if (objects != null && objects.isDirectory()) {
                    return module;
                }
            }
        }

        return modules[0];
    }

    public static PrismSerializer<String> getSerializer(PrismContext prismContext) {
        return prismContext.xmlSerializer()
                .options(SerializationOptions.createSerializeReferenceNames());
    }

    public static PrismParser createParser(PrismContext ctx, InputStream data) {
        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(data).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public static PrismParser createParser(PrismContext ctx, String xml) {
        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(xml).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public static String serialize(PrismContext prismContext, Object object) throws SchemaException {
        final QName fakeQName = new QName(PrismConstants.NS_TYPES, "object");

        PrismSerializer<String> serializer = getSerializer(prismContext);

        String result;
        if (object instanceof ObjectType) {
            ObjectType ot = (ObjectType) object;
            result = serializer.serialize(ot.asPrismObject());
        } else if (object instanceof PrismObject) {
            result = serializer.serialize((PrismObject<?>) object);
        } else if (object instanceof OperationResult) {
            LocalizationService localizationService = new LocalizationServiceImpl();
            Function<LocalizableMessage, String> resolveKeys = msg -> localizationService.translate(msg, Locale.US);
            OperationResultType operationResultType = ((OperationResult) object).createOperationResultType(resolveKeys);
            result = serializer.serializeAnyData(operationResultType, fakeQName);
        } else {
            result = serializer.serializeAnyData(object, fakeQName);
        }

        return result;
    }

    public static List<ObjectTypes> getConcreteObjectTypes() {
        List<ObjectTypes> rv = new ArrayList<>();
        for (ObjectTypes t : ObjectTypes.values()) {
            if (!Modifier.isAbstract(t.getClassDefinition().getModifiers())) {
                rv.add(t);
            }
        }
        return rv;
    }

    public static void forceSaveAndRefresh(Project project, VirtualFile file) {
        file.refresh(false, true);

        FileEditor[] editors = FileEditorManager.getInstance(project).getEditors(file);
        for (FileEditor editor : editors) {
            if (!(editor instanceof TextEditor)) {
                continue;
            }

            TextEditor textEditor = (TextEditor) editor;
            Document doc = textEditor.getEditor().getDocument();
            FileDocumentManager.getInstance().saveDocument(doc);
        }
    }
}
