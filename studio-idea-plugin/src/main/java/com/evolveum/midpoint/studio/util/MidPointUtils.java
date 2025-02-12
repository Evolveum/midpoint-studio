package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.studio.client.ClientException;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.ServiceFactory;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.ToolbarAction;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.facet.FacetManager;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.patterns.XmlPatterns;
import com.intellij.patterns.XmlTagPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.DisposeAwareRunnable;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.Color;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointUtils {

    public static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static final Comparator<ObjectTypes> OBJECT_TYPES_COMPARATOR =
            Comparator.comparing(o -> o.name());

    public static final Comparator<ObjectType> OBJECT_TYPE_COMPARATOR =
            (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(
                    getOrigFromPolyString(o1.getName()),
                    getOrigFromPolyString(o2.getName())
            );

    private static final Logger LOG = Logger.getInstance(MidPointUtils.class);

    private static final Random RANDOM = new Random();

    @Deprecated
    public static final PrismContext DEFAULT_PRISM_CONTEXT = ServiceFactory.DEFAULT_PRISM_CONTEXT;

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("\\$(t|T|s|e|n|o)");

    private static final String[] NAMES;

    public static final Set<String> NAMESPACES;

    static {
        NAMESPACES = Set.of(
                SchemaConstantsGenerated.NS_COMMON,
                SchemaConstantsGenerated.NS_QUERY,
                SchemaConstantsGenerated.NS_SCRIPTING);

        List<String> names = new ArrayList<>();
        for (ObjectTypes t : ObjectTypes.values()) {
            if (Modifier.isAbstract(t.getClassDefinition().getModifiers())) {
                continue;
            }

            names.add(t.getElementName().getLocalPart());
        }

        NAMES = names.toArray(new String[0]);
    }

    public static Color generateAwtColor() {
        float hue = RANDOM.nextFloat();
        // Saturation between 0.1 and 0.3
        float saturation = (RANDOM.nextInt(2000) + 1000) / 10000f;

        return Color.getHSBColor(hue, saturation, 0.9f);
    }

public static LookupElement buildOidLookupElement(String name, String oid, QName type, String source, int priority) {
        LookupElementBuilder builder = buildOidLookupElement(name, oid, source);
        if (type != null) {
            builder = builder.withInsertHandler(new OidTypeInsertHandler(type));
        }
        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, priority);
    }

    public static LookupElementBuilder buildOidLookupElement(String name, String oid, String source) {
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
        // previously was "MidPointSettings" (simple class name for MidPointConfiguration)
        // we left it as is, because we don't want to change the key that is used to search keychain for existing credentials
        return new CredentialAttributes(CredentialAttributesKt
                .generateServiceName("MidPointSettings", key));
    }

    public static void publishException(Project project, Environment env, Class clazz, String notificationKey, String msg, Exception ex) {
        MidPointService mm = MidPointService.get(project);
        mm.printToConsole(env, clazz, msg + ". Reason: " + ex.getMessage());

        publishExceptionNotification(project, env, clazz, notificationKey, msg, ex);
    }

    public static void publishExceptionNotification(Project project, Environment env, Class clazz, String key, String message, Exception ex) {
        publishExceptionNotification(project, env, clazz, key, message, ex, new NotificationAction[]{});
    }

    public static void publishExceptionNotification(Project project, Environment env, Class clazz, String key, String message, Exception ex, NotificationAction... actions) {
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

        MidPointUtils.publishNotification(project, key, "Error", msg, NotificationType.ERROR, list.toArray(new NotificationAction[list.size()]));

        if (LOG.isTraceEnabled()) {
            LOG.trace(msg);
            LOG.trace(ex);
        } else {
            LOG.debug(msg);
        }
    }

    public static void publishNotification(Project project, String key, String title, String content, NotificationType type) {
        publishNotification(project, key, title, content, type, (NotificationAction[]) null);
    }

    public static void publishNotification(Project project, String key, String title, String content, NotificationType type,
                                           NotificationAction... actions) {
        Notification notification = new Notification(key, title, content, type);
        if (actions != null) {
            Arrays.stream(actions).filter(a -> a != null).forEach(a -> notification.addAction(a));
        }

        Notifications.Bus.notify(notification, project);
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
            MidPointUtils.publishNotification(project, key, "Error",
                    message + ", reason: " + ex.getMessage(), NotificationType.ERROR, action);
        }

        if (project != null) {
            MidPointService manager = MidPointService.get(project);
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

    public static AnAction createAnAction(
            String text, Icon icon, Consumer<AnActionEvent> actionPerformed, Consumer<AnActionEvent> update) {

        return createAnAction(text, text, icon, actionPerformed, update);
    }

    public static AnAction createAnAction(
            String text, String description, Icon icon, Consumer<AnActionEvent> actionPerformed, Consumer<AnActionEvent> update) {

        return new ToolbarAction(text, description, icon, actionPerformed) {

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
    public static JXTreeTable createTable2(TreeTableModel model, TableColumnModelExt columnModel, boolean disableHack) {
        return createTable2(model, columnModel, disableHack, null);
    }

    @Experimental
    public static <R> JXTreeTable createTable2(TreeTableModel model, TableColumnModelExt columnModel, boolean disableHack,
            Consumer<JXTreeTable> tableCustomizer) {

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

        if (tableCustomizer != null) {
            tableCustomizer.accept(table);
        }

        table.packAll();

        return table;
    }

    private static <R> void applyColumnDefinitions(List<TreeTableColumnDefinition<R, ?>> columnDefinitions, JXTreeTable table) {
        for (int i = 0; i < columnDefinitions.size(); i++) {
            TreeTableColumnDefinition<R, ?> columnDef = columnDefinitions.get(i);

            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnDef.getSize());
            if (columnDef.getMinimalSize() != null) {
                column.setMinWidth(columnDef.getMinimalSize());
            }
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

    public static String formatTimePrecise(Long time) {
        if (time == null) {
            return "";
        } else {
            return String.format(Locale.US, "%.2f", time / 1000.0);
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

    public static boolean isTagMatchingNameOrType(XmlTag tag, QName name, QName type) {
        if (tag == null) {
            return false;
        }

        QName realName = createQName(tag);
        if (name != null && name.equals(realName)) {
            return true;
        }

        QName realType = elementXsiType(tag);
        if (type != null && type.equals(realType)) {
            return true;
        }

        return false;
    }

    public static QName elementXsiType(XmlTag tag) {
        if (tag == null) {
            return null;
        }

        XmlAttribute xsiType = tag.getAttribute("type", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
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
            if (columnDefinition.getMinimalSize() != null) {
                column.setMinWidth(columnDefinition.getMinimalSize());
            }
            model.addColumn(column);
        }
        return model;
    }

    public static boolean isObjectTypeElement(XmlTag tag) {
        return isObjectTypeElement(tag, true);
    }

    public static boolean isObjectTypeElement(XmlTag tag, boolean namespaceAware) {
        if (tag == null) {
            return false;
        }

        QName name = new QName(tag.getNamespace(), tag.getLocalName());
        for (ObjectTypes type : ObjectTypes.values()) {
            if (name.equals(type.getElementName())) {
                return true;
            }

            if (!namespaceAware && type.getElementName().getLocalPart().equalsIgnoreCase(name.getLocalPart())) {
                return true;
            }
        }

        return false;
    }

    public static List<VirtualFile> filterXsdFiles(VirtualFile[] files) {
        return filterXmlFiles(files, "xsd");
    }

    public static List<VirtualFile> filterXmlFiles(VirtualFile[] files) {
        return filterXmlFiles(files, XmlFileType.DEFAULT_EXTENSION);
    }

    public static List<VirtualFile> filterZipFiles(VirtualFile[] files) {
        return filterXmlFiles(files, "zip");
    }

    public static List<VirtualFile> filterXmlFiles(VirtualFile[] files, String extension) {
        List<VirtualFile> result = new ArrayList<>();
        if (files == null) {
            return result;
        }

        for (VirtualFile selected : files) {
            if (selected.isDirectory()) {
                VfsUtilCore.iterateChildrenRecursively(
                        selected,
                        file -> file.isDirectory() || extension.equalsIgnoreCase(file.getExtension()),
                        file -> {
                            if (!file.isDirectory() && !result.contains(file)) {
                                result.add(file);
                            }
                            return true;
                        });
            } else if (extension.equalsIgnoreCase(selected.getExtension())) {
                if (!result.contains(selected)) {
                    result.add(selected);
                }
            }
        }

        return result;
    }

    @RequiresBackgroundThread
    public static boolean hasMidPointFacet(Project project) {
        if (project == null) {
            return false;
        }

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
                                                .withLocalName(NAMES)))).accepts(element);
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
        Computable<Boolean> computable = () -> {
            if (!isVisibleWithMidPointFacet(evt)) {
                return false;
            }

            return isEnvironmentAndFileSelected(evt);
        };

        if (ApplicationManager.getApplication().isDispatchThread()) {
            return computable.compute();
        } else {
            return ApplicationManager.getApplication().runReadAction(computable);
        }
    }

    public static boolean isMidpointFile(PsiFile file) {
        if (file == null) {
            return false;
        }

        if (!(file instanceof XmlFile xmlFile)) {
            return false;
        }

        if (xmlFile.getRootTag() == null) {
            return false;
        }

        String namespace = xmlFile.getRootTag().getNamespace();
        return NAMESPACES.contains(namespace);
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

    public static List<MidPointObject> parseText(Project project, String text, VirtualFile file, String notificationKey) {
        try {
            File ioFile = file != null ? VfsUtil.virtualToIoFile(file) : null;
            return ClientUtils.parseText(text, ioFile);
        } catch (RuntimeException ex) {
            String msg = "Couldn't parse text '" + org.apache.commons.lang3.StringUtils.abbreviate(text, 10) + "'";

            if (notificationKey != null) {
                MidPointUtils.publishExceptionNotification(project, null, MidPointUtils.class, notificationKey, msg, ex);
            }

            return new ArrayList<>();
        }
    }

    public static List<MidPointObject> parseProjectFile(Project project, VirtualFile file, String notificationKey) {
        try {
            return ClientUtils.parseProjectFile(VfsUtil.virtualToIoFile(file), file.getCharset());
        } catch (IOException ex) {
            if (notificationKey != null) {
                MidPointUtils.publishExceptionNotification(project, null, MidPointUtils.class, notificationKey,
                        "Couldn't parse file " + (file != null ? file.getName() : null) + " to DOM", ex);
            }
            return new ArrayList<>();
        }
    }

    public static String upgradeTaskToUseActivities(String taskXml) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        org.w3c.dom.Document doc = setupDocument(taskXml);

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                return "c".equals(prefix) ? SchemaConstantsGenerated.NS_COMMON : null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return SchemaConstantsGenerated.NS_COMMON.equals(namespaceURI) ? "c" : null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return SchemaConstantsGenerated.NS_COMMON.equals(namespaceURI) ? new org.bouncycastle.util.Arrays.Iterator(new String[]{"c"}) : null;
            }
        });

        //Get first match
        Boolean exists = (Boolean) xpath.evaluate("/c:task/c:activity", doc, XPathConstants.BOOLEAN);
        if (exists) {
            return taskXml;
        }

        String xml = transformTask(doc, "task-transformation.xslt");
        if (Objects.equals(taskXml, xml)) {
            return xml;
        }

        doc = setupDocument(xml);
        xml = transformTask(doc, "task-cleanup.xslt");

        return xml;
    }

    private static org.w3c.dom.Document setupDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
        doc.normalize();

        return doc;
    }

    private static String transformTask(org.w3c.dom.Document doc, String stylesheet) throws TransformerException, IOException {
        try (InputStream is = MidPointUtils.class.getClassLoader().getResourceAsStream(stylesheet)) {
            StreamSource xsl = new StreamSource(is);

            TransformerErrorListener tel = new TransformerErrorListener();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer(xsl);
            trans.setErrorListener(tel);
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setParameter(OutputKeys.INDENT, "yes");
            trans.setParameter(OutputKeys.ENCODING, "utf-8");

            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(doc), new StreamResult(sw));

            String output = sw.toString();
            if (tel.isErrorOrFatal()) {
                throw new TransformerException("Found these problems:\n" + tel.dumpAllMessages());
            }

            return output;
        }
    }

    public static String updateObjectRootElementToObject(String objectXml) {
        if (objectXml == null) {
            return null;
        }

        org.w3c.dom.Document doc = DOMUtil.parseDocument(objectXml);
        Element rootNode = doc.getDocumentElement();
        QName rootName = new QName(rootNode.getNamespaceURI(), rootNode.getLocalName());

        if (rootName.equals(ObjectTypes.OBJECT) && rootNode.hasAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type")) {
            return objectXml;
        }

        Node previousRoot = doc.removeChild(rootNode);

        Element root = DOMUtil.createElement(doc, SchemaConstantsGenerated.C_OBJECT);
        doc.appendChild(root);

        NodeList list = previousRoot.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node copied = doc.importNode(list.item(i), true);
            root.appendChild(copied);
        }

        NamedNodeMap attributes = previousRoot.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node node = attributes.item(i);
                if (node.getNodeType() != Node.ATTRIBUTE_NODE) {
                    continue;
                }

                Attr attr = (Attr) doc.importNode(node, true);
                root.setAttributeNodeNS(attr);
            }
        }

        // if there's no xsi:type in root, we'll try to figure out and add it
        if (!root.hasAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type")) {
            ObjectTypes type = null;
            for (ObjectTypes ot : ObjectTypes.values()) {
                if (rootName.equals(ot.getElementName())) {
                    type = ot;
                    break;
                }
            }

            if (type == null) {
                return objectXml;
            }

            root.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:type", type.getTypeQName().getLocalPart());
        }

        return ClientUtils.serializeDOMToString(doc);
    }

    public static void subscribeToEnvironmentChange(Project project, Consumer<Environment> refreshFunction) {
        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifier() {

            @Override
            public void environmentChanged(Environment oldEnv, Environment newEnv) {
                refreshFunction.accept(newEnv);
            }
        });

        ApplicationManager.getApplication().invokeLater(() -> {
            EnvironmentService service = project.getService(EnvironmentService.class);
            refreshFunction.accept(service.getSelected());
        });
    }

    private static class TransformerErrorListener implements ErrorListener {

        private List<String> warnings = new ArrayList<>();

        private List<String> errors = new ArrayList<>();

        private List<String> fatalErrors = new ArrayList<>();

        @Override
        public void warning(TransformerException exception) throws TransformerException {
            warnings.add(createMessage("Warning", exception));
        }

        @Override
        public void error(TransformerException exception) throws TransformerException {
            errors.add(createMessage("Error", exception));
        }

        @Override
        public void fatalError(TransformerException exception) throws TransformerException {
            fatalErrors.add(createMessage("Fatal error", exception));
        }

        private String createMessage(String level, TransformerException ex) {
            return level + ": Position[" + ex.getLocationAsString() + "]: " + ex.getMessage();
        }

        private boolean isErrorOrFatal() {
            return !errors.isEmpty() || !fatalErrors.isEmpty();
        }

        private String dumpAllMessages() {
            List<String> all = new ArrayList<>();
            all.addAll(warnings);
            all.addAll(errors);
            all.addAll(fatalErrors);

            return StringUtils.join(all, "\n");
        }
    }

    public static XmlTagPattern.Capture typesTag(String localName) {
        return qualifiedTag(localName, SchemaConstantsGenerated.NS_TYPES);
    }

    public static XmlTagPattern.Capture commonTag(String localName) {
        return qualifiedTag(localName, SchemaConstantsGenerated.NS_COMMON);
    }

    public static XmlTagPattern.Capture annotationTag(String localName) {
        return qualifiedTag(localName, SchemaConstantsGenerated.NS_ANNOTATION);
    }

    public static XmlTagPattern.Capture qualifiedTag(QName name) {
        return XmlPatterns.xmlTag().withLocalName(name.getLocalPart()).withNamespace(name.getNamespaceURI());
    }

    public static XmlTagPattern.Capture qualifiedTag(String localName, String namespace) {
        return XmlPatterns.xmlTag().withLocalName(localName).withNamespace(namespace);
    }

    public static Icon createEnvironmentIcon(Color color) {
        if (color == null) {
            return null;
        }

        return new ColorIcon(24, 14, 24, 14, color, true);
    }

    public static <T extends Enum> String createKeyForEnum(T value) {
        if (value == null) {
            return null;
        }

        return value.getClass().getSimpleName() + "." + value.name();
    }

    public static int showConfirmationDialog(
            Project project, String message, String title, String yesText, String noText) {

        AtomicInteger result = new AtomicInteger(0);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            Component comp = WindowManager.getInstance().suggestParentWindow(project);

            JComponent source;
            if (comp instanceof JComponent) {
                source = (JComponent) comp;
            } else if (comp instanceof JWindow) {
                source = ((JWindow) comp).getRootPane();
            } else if (comp instanceof JFrame) {
                source = ((JFrame) comp).getRootPane();
            } else {
                throw new IllegalStateException("Couldn't find parent component for dialog");
            }

            int r = Messages.showConfirmationDialog(source, message, title, yesText, noText);

            result.set(r);
        });

        return result.get();
    }


    /**
     * Returns type of object from ObjectReferenceType xml tag PSI element (from type attribute).
     */
    public static ObjectTypes getTypeFromReference(XmlTag tag) {
        String xmlType = tag.getAttributeValue("type");
        if (xmlType == null) {
            return null;
        }

        String localPart = QNameUtil.parsePrefixedName(xmlType).localName();
        return Arrays.stream(ObjectTypes.values())
                .filter(t -> t.getTypeQName().getLocalPart().equals(localPart))
                .findFirst()
                .orElse(null);
    }

    public static boolean isDevelopmentMode(boolean enabled) {
        boolean internal = ApplicationManager.getApplication().isInternal();

        return enabled && internal;
    }

    public static MidPointObject expand(MidPointObject object, Expander expander) {
        VirtualFile file = object.getFile() != null ? VfsUtil.findFileByIoFile(object.getFile(), true) : null;
        String content = expander.expand(object.getContent(), file);

        return ClientUtils.parseText(content, object.getFile()).get(0);
    }

    public static String getDisplayNameOrName(ObjectType object) {
        if (object == null) {
            return null;
        }

        PolyStringType name = ObjectTypeUtil.getDisplayName(object);
        if (name == null) {
            name = object.getName();
        }

        return name != null ? name.getOrig() : null;
    }
}
