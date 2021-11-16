package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.RefreshAction;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskUpgradeTask extends Task.Backgroundable {

    public static final String NOTIFICATION_KEY = "Transform Task Action";

    private static final Logger LOG = Logger.getInstance(TaskUpgradeTask.class);

    private AnActionEvent event;

    private State state = new State();

    public TaskUpgradeTask(AnActionEvent event) {
        super(event.getProject(), "Refresh From Server", true);

        this.event = event;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        LOG.info("Starting task: " + getClass().getName());

        new RunnableUtils.PluginClasspathRunnable() {

            @Override
            public void runWithPluginClassLoader() {
                doRun(indicator);
            }
        }.run();
    }

    private void doRun(ProgressIndicator indicator) {
        if (getProject() == null) {
            return;
        }

        indicator.setIndeterminate(false);

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        AtomicInteger result = new AtomicInteger(0);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            InputEvent inputEvent = event.getInputEvent();
            Component comp = inputEvent != null ? inputEvent.getComponent() : null;

            JComponent source;
            if (comp instanceof JComponent) {
                source = (JComponent) comp;
            } else {
                JWindow w;
                if (comp instanceof JWindow) {
                    w = (JWindow) comp;
                } else {
                    w = (JWindow) WindowManager.getInstance().suggestParentWindow(getProject());
                }

                source = w.getRootPane();
            }

            int r = Messages.showConfirmationDialog(source, "Are you sure you want to upgrade " + toProcess.size()
                    + " tasks?", "Confirm action", "Upgrade", "Cancel");

            result.set(r);
        });

        if (result.get() == Messages.NO) {
            return;
        }

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, getTitle(),
                    "No files matched for " + getTitle() + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(indicator, toProcess);
    }

    private void processFiles(ProgressIndicator indicator, List<VirtualFile> files) {
        MidPointService mm = MidPointService.getInstance(getProject());

        int current = 0;
        for (VirtualFile file : files) {
            ProgressManager.checkCanceled();

            current++;
            indicator.setFraction((double) current / files.size());

            List<MidPointObject> objects = new ArrayList<>();

            RunnableUtils.runWriteActionAndWait(() -> {
                MidPointUtils.forceSaveAndRefresh(getProject(), file);

                List<MidPointObject> obj = MidPointUtils.parseProjectFile(getProject(), file, NOTIFICATION_KEY);
                obj = ClientUtils.filterObjectTypeOnly(obj);

                objects.addAll(obj);
            });

            if (objects.isEmpty()) {
                state.incrementSkipped();
                mm.printToConsole(null, RefreshAction.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
                continue;
            }

            List<String> newObjects = new ArrayList<>();

            for (MidPointObject object : objects) {
                ProgressManager.checkCanceled();

                if (!ObjectTypes.TASK.equals(object.getType())) {
                    newObjects.add(object.getContent());
                    state.incrementSkipped();
                }

                try {
//                    Thread thread = Thread.currentThread();
//
//                    ClassLoader cl = thread.getContextClassLoader();
//                    try {
//                        thread.setContextClassLoader(RunnableUtils.class.getClassLoader());

                        String newContent = TaskUpgradeTask.this.transformTask(object.getContent());
                        newObjects.add(newContent);
//                    } finally {
//                        thread.setContextClassLoader(cl);
//                    }

                    state.incrementProcessed();
                } catch (Exception ex) {
                    state.incrementFailed();
                    newObjects.add(object.getContent());

                    mm.printToConsole(null, RefreshAction.class, "Error upgrading task"
                            + object.getName() + "(" + object.getOid() + ")", ex);
                }
            }

            RunnableUtils.runWriteActionAndWait(() -> {
                try (Writer writer = new OutputStreamWriter(file.getOutputStream(this), file.getCharset())) {
                    if (newObjects.size() > 1) {
                        writer.write(ClientUtils.OBJECTS_XML_PREFIX);
                        writer.write('\n');
                    }

                    for (String obj : newObjects) {
                        writer.write(obj);
                    }

                    if (newObjects.size() > 1) {
                        writer.write(ClientUtils.OBJECTS_XML_SUFFIX);
                        writer.write('\n');
                    }
                } catch (IOException ex) {
                    state.incrementFailed();

                    mm.printToConsole(null, RefreshAction.class, "Failed to write upgraded file " + file.getPath(), ex);
                }
            });
        }

        NotificationType type = state.missing > 0 || state.failed > 0 || state.skipped > 0 ? NotificationType.WARNING : NotificationType.INFORMATION;
        String msg = buildFinishedNotification();

        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, "Refresh Action", msg, type);
    }

    private String buildFinishedNotification() {
        StringBuilder msg = new StringBuilder();

        msg.append("Upgraded ").append(state.processed).append(" objects<br/>");
        msg.append("Missing ").append(state.missing).append(" objects<br/>");
        msg.append("Failed to upgrade ").append(state.failed).append(" objects<br/>");
        msg.append("Skipped ").append(state.skipped).append(" files");

        return msg.toString();
    }

    @Override
    public void onCancel() {
        LOG.info("Task upgrade action was cancelled");

        String msg = "Processed before cancel requested:<br/>" + buildFinishedNotification();
        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, "Task Upgrade Action Canceled", msg, NotificationType.WARNING);
    }

    private String transformTask(String xml) throws TransformerException, ParserConfigurationException, IOException, SAXException {
        try (InputStream xsltStream = TaskUpgradeTask.class.getClassLoader().getResourceAsStream("/task-transformation.xslt")) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
            doc.normalize();

            StreamSource xsl = new StreamSource(xsltStream);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer(xsl);
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setParameter(OutputKeys.INDENT, "yes");
            trans.setParameter(OutputKeys.ENCODING, "utf-8");

            StringWriter sw = new StringWriter();

            trans.transform(new DOMSource(doc), new StreamResult(sw));

            return sw.toString();
        }
    }

    private static class State {

        private int processed;

        private int missing;

        private int failed;

        private int skipped;

        void incrementProcessed() {
            processed++;
        }

        void incrementMissing() {
            missing++;
        }

        void incrementFailed() {
            failed++;
        }

        void incrementSkipped() {
            skipped++;
        }
    }
}
