package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileAction extends AnAction {

    public static final String ACTION_NAME = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    public static final Map<Class<? extends ObjectType>, List<ItemPath>> CLEANUP_PATHS = new HashMap<>();

    static {
        CLEANUP_PATHS.put(ObjectType.class, Arrays.asList(
                ItemPath.create(ObjectType.F_METADATA),
                ItemPath.create(ObjectType.F_FETCH_RESULT),
                ItemPath.create(ObjectType.F_OPERATION_EXECUTION),
                ItemPath.create(ObjectType.F_DIAGNOSTIC_INFORMATION)
        ));

        CLEANUP_PATHS.put(AssignmentHolderType.class, Arrays.asList(
                ItemPath.create(AssignmentHolderType.F_ITERATION),
                ItemPath.create(AssignmentHolderType.F_ITERATION_TOKEN)
        ));

        CLEANUP_PATHS.put(FocusType.class, Arrays.asList(
                ItemPath.create(FocusType.F_ASSIGNMENT, AssignmentType.F_METADATA),
                ItemPath.create(FocusType.F_ROLE_MEMBERSHIP_REF),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_EFFECTIVE_STATUS),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_ENABLE_TIMESTAMP),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_DISABLE_TIMESTAMP),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_DISABLE_REASON),
                ItemPath.create(FocusType.F_CREDENTIALS, CredentialsType.F_PASSWORD, PasswordType.F_METADATA)
        ));

        CLEANUP_PATHS.put(TaskType.class, Arrays.asList(
                ItemPath.create(TaskType.F_RESULT),
                ItemPath.create(TaskType.F_RESULT_STATUS),
                ItemPath.create(TaskType.F_OPERATION_STATS),
                ItemPath.create(TaskType.F_COMPLETION_TIMESTAMP),
                ItemPath.create(TaskType.F_LAST_RUN_FINISH_TIMESTAMP),
                ItemPath.create(TaskType.F_LAST_RUN_START_TIMESTAMP),
                ItemPath.create(new QName(SchemaConstants.NS_C, "workState")),  // TaskType.F_WORK_STATE, not available in 4.4 schema
                ItemPath.create(TaskType.F_TASK_IDENTIFIER),
                ItemPath.create(TaskType.F_PROGRESS)
        ));
    }

    public CleanupFileAction() {
        super(ACTION_NAME);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.isMidpointObjectFileSelected(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));
        if (selectedFiles == null || selectedFiles.length == 0) {
            MidPointUtils.publishNotification(e.getProject(), NOTIFICATION_KEY, ACTION_NAME,
                    "No files selected for cleanup", NotificationType.WARNING);
            return;
        }

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(e.getProject(), NOTIFICATION_KEY, ACTION_NAME,
                    "No files matched for cleanup (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(e, toProcess);
    }

    private void processFiles(AnActionEvent evt, List<VirtualFile> files) {
        for (VirtualFile file : files) {
            RunnableUtils.runWriteActionAndWait(() -> {
                MidPointUtils.forceSaveAndRefresh(evt.getProject(), file);

                Document doc = null;
                try (Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()))) {
                    String xml = IOUtils.toString(in);

                    doc = DOMUtil.parseDocument(xml);
                } catch (IOException ex) {
                    ex.printStackTrace(); // todo fix
//                    publishException(mm, "Exception occurred when loading file '" + file.getName() + "'", ex);
                }

                if (doc == null) {
                    return;
                }

                processObject(doc.getDocumentElement());

                try (Writer w = new BufferedWriter(new OutputStreamWriter(file.getOutputStream(this), file.getCharset()))) {
                    String xml = DOMUtil.serializeDOMToString(doc);

                    IOUtils.write(xml, w);
                } catch (IOException ex) {
                    ex.printStackTrace(); // todo fix
                }
            });
        }
    }

    private void processObject(Element element) {
        QName name = DOMUtil.getQName(element);

        ObjectTypes type = null;
        for (ObjectTypes ot : ObjectTypes.values()) {
            if (ot.getElementName().equals(name)) {
                type = ot;
                break;
            }
        }

        if (type == null) {
            return;
        }

        boolean cleaned = false;
        for (Class<? extends ObjectType> clazz : CLEANUP_PATHS.keySet()) {
            if (!clazz.isAssignableFrom(type.getClassDefinition())) {
                continue;
            }

            List<ItemPath> paths = CLEANUP_PATHS.get(clazz);
            for (ItemPath path : paths) {
                cleaned = cleaned | cleanupObject(element, path);
            }
        }

        if (cleaned) {
            DOMUtil.normalize(element, false);
        }
    }

    private boolean cleanupObject(Element obj, ItemPath path) {
        if (path.isEmpty()) {
            obj.getParentNode().removeChild(obj);
            return true;
        }

        boolean cleaned = false;
        List<Element> elements = DOMUtil.getChildElements(obj, path.firstToName());
        for (Element e : elements) {
            cleaned = cleaned | cleanupObject(e, path.rest());
        }

        return cleaned;
    }
}
