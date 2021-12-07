package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectType;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DiffRemoteAction extends BackgroundAction {

    public static final String NOTIFICATION_KEY = "Diff Remote Action";

    public DiffRemoteAction() {
        super("Diff Remote", AllIcons.Actions.Diff, NOTIFICATION_KEY);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.shouldEnableAction(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        if (evt.getProject() == null) {
            return;
        }

        indicator.setIndeterminate(false);

        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        Environment env = em.getSelected();

        VirtualFile[] selectedFiles = UIUtil.invokeAndWaitIfNeeded(() -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(evt.getProject(), NOTIFICATION_KEY, getTaskTitle(),
                    "No files matched for " + getTaskTitle() + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(evt, indicator, env, toProcess);
    }

    private void processFiles(AnActionEvent evt, ProgressIndicator indicator, Environment env, List<VirtualFile> files) {
        MidPointService mm = MidPointService.getInstance(evt.getProject());

        MidPointClient client = new MidPointClient(evt.getProject(), env);

        int skipped = 0;
        int missing = 0;
        AtomicInteger diffed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        int current = 0;
        for (VirtualFile file : files) {
            ProgressManager.checkCanceled();

            current++;
            indicator.setFraction((double) current / files.size());

            List<MidPointObject> objects = new ArrayList<>();

            RunnableUtils.runWriteActionAndWait(() -> {
                MidPointUtils.forceSaveAndRefresh(evt.getProject(), file);

                List<MidPointObject> obj = MidPointUtils.parseProjectFile(evt.getProject(), file, NOTIFICATION_KEY);
                obj = ClientUtils.filterObjectTypeOnly(obj);

                objects.addAll(obj);
            });

            if (objects.isEmpty()) {
                skipped++;
                mm.printToConsole(env, DiffRemoteAction.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
                continue;
            }

            Map<String, MidPointObject> remoteObjects = new HashMap<>();

            for (MidPointObject object : objects) {
                ProgressManager.checkCanceled();

                try {
                    MidPointObject newObject = client.get(object.getType().getClassDefinition(), object.getOid(), new SearchOptions().raw(true));
                    if (newObject == null) {
                        missing++;

                        mm.printToConsole(env, DiffRemoteAction.class, "Couldn't find object "
                                + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ").");

                        continue;
                    }

                    MidPointObject obj = MidPointObject.copy(object);
                    obj.setContent(newObject.getContent());
                    remoteObjects.put(obj.getOid(), obj);

                    diffed.incrementAndGet();
                } catch (Exception ex) {
                    failed.incrementAndGet();

                    mm.printToConsole(env, DiffRemoteAction.class, "Error getting object"
                            + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ")", ex);
                }
            }

            RunnableUtils.runWriteActionAndWait(() -> {

                Writer writer = null;
                VirtualFile vf = null;
                try {
                    List<String> deltas = new ArrayList<>();

                    for (MidPointObject local : objects) {
                        MidPointObject remote = remoteObjects.get(local.getOid());
                        if (remote == null) {
                            continue;
                        }

                        // todo expand local content before its used for comparing
                        PrismObject localObject = client.parseObject(local.getContent());
                        PrismObject remoteObject = client.parseObject(remote.getContent());

                        ObjectDeltaObjectType odo = new ObjectDeltaObjectType();
                        odo.setOldObject((ObjectType) localObject.asObjectable());
                        odo.setNewObject((ObjectType) remoteObject.asObjectable());

                        String xml = client.serialize(odo);
                        deltas.add(xml);
                    }

                    vf = FileUtils.createScratchFile(evt.getProject(), env);

                    writer = new OutputStreamWriter(vf.getOutputStream(this), vf.getCharset());

                    if (deltas.size() > 1) {
                        writer.write(ClientUtils.DELTAS_XML_PREFIX);
                        writer.write('\n');
                    }

                    for (String obj : deltas) {
                        writer.write(obj);
                    }

                    if (deltas.size() > 1) {
                        writer.write(ClientUtils.DELTAS_XML_SUFFIX);
                        writer.write('\n');
                    }
                } catch (Exception ex) {
                    failed.incrementAndGet();

                    mm.printToConsole(env, DiffRemoteAction.class, "Failed to compare file " + file.getPath(), ex);
                } finally {
                    IOUtils.closeQuietly(writer);
                }

                MidPointUtils.openFile(evt.getProject(), vf);
            });
        }

        NotificationType type = missing > 0 || failed.get() > 0 || skipped > 0 ? NotificationType.WARNING : NotificationType.INFORMATION;

        StringBuilder msg = new StringBuilder();
        msg.append("Compared ").append(diffed.get()).append(" objects<br/>");
        msg.append("Missing ").append(missing).append(" objects<br/>");
        msg.append("Failed to compare ").append(failed.get()).append(" objects<br/>");
        msg.append("Skipped ").append(skipped).append(" files");
        MidPointUtils.publishNotification(evt.getProject(), NOTIFICATION_KEY, getTaskTitle(), msg.toString(), type);
    }
}
