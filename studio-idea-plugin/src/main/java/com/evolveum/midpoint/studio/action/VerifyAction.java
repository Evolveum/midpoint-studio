package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.validator.ObjectValidator;
import com.evolveum.midpoint.schema.validator.ValidationItem;
import com.evolveum.midpoint.schema.validator.ValidationResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class VerifyAction extends AnAction {

    public static final String ACTION_NAME = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.isMidpointObjectFileSelected(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));
        if (selectedFiles == null || selectedFiles.length == 0) {
            MidPointUtils.publishNotification(project, NOTIFICATION_KEY, ACTION_NAME,
                    "No files selected for cleanup", NotificationType.WARNING);
            return;
        }

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(project, NOTIFICATION_KEY, ACTION_NAME,
                    "No files matched for verification (xml)", NotificationType.WARNING);
            return;
        }

        MidPointService mm = MidPointService.getInstance(project);

        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment env = em.getSelected();

        mm.focusConsole();

        mm.printToConsole(env, VerifyAction.class, "Starting verification using midpoint schema bundled in MidPoint Studio.");

        processFiles(e, mm, env, toProcess);

        mm.printToConsole(env, VerifyAction.class, "Verification finished");
    }

    private void processFiles(AnActionEvent evt, MidPointService mm, Environment env, List<VirtualFile> files) {
        ObjectValidator validator = new ObjectValidator(MidPointUtils.DEFAULT_PRISM_CONTEXT);
        validator.setAllWarnings();

        MidPointClient client = new MidPointClient(evt.getProject(), env);

        for (VirtualFile file : files) {
            RunnableUtils.runReadAction(() -> {
                try {
                    List<MidPointObject> objects = MidPointUtils.parseProjectFile(evt.getProject(), file, NOTIFICATION_KEY);

                    for (MidPointObject obj : objects) {
                        try {
                            PrismObject object = client.parseObject(obj.getContent());

                            ValidationResult validationResult = validator.validate(object);
                            for (ValidationItem validationItem : validationResult.getItems()) {
                                String msg = buildValidationItem(object, validationItem);
                                mm.printToConsole(env, VerifyAction.class, msg);
                            }
                        } catch (Exception ex) {
                            mm.printToConsole(env, VerifyAction.class,
                                    "Couldn't parse object '" + obj.getName() + "'(" + obj.getType() +
                                            ") from file '" + file.getPath() + "', reason: " + ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    mm.printToConsole(env, VerifyAction.class,
                            "Couldn't parse objects from file '" + file.getPath() + "', reason: " + ex.getMessage());
                }
            });
        }
    }

    private <O extends ObjectType> String buildValidationItem(PrismObject<O> object, ValidationItem validationItem)
            throws IOException {

        StringBuilder sb = new StringBuilder();

        if (validationItem.getStatus() != null) {
            sb.append(validationItem.getStatus().toString());
            sb.append(" ");
        } else {
            sb.append("INFO ");
        }
        sb.append(object.toString());
        sb.append(" ");
        if (validationItem.getItemPath() != null) {
            sb.append(validationItem.getItemPath().toString());
            sb.append(" ");
        }
        writeMessage(sb, validationItem.getMessage());

        return sb.toString();
    }

    private void writeMessage(StringBuilder sb, LocalizableMessage message) throws IOException {
        if (message == null) {
            return;
        }

        sb.append(message.getFallbackMessage());
    }
}
