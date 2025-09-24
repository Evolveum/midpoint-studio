package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Dominik.
 */
public class ConvertAction extends AnAction {

    private static final Logger log = LoggerFactory.getLogger(ConvertAction.class);

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String id = ActionManager.getInstance().getId(this);

        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (id != null) {
            switch (id) {
                case "MidPoint.Action.Converter.ConvertToXml":
                    convert(project, file, "xml");
                    break;

                case "MidPoint.Action.Converter.ConvertToJson":
                    convert(project, file, "json");
                    break;

                case "MidPoint.Action.Converter.ConvertToYaml":
                    convert(project, file, "yaml");
                    break;

                default:
                    log.error("Unknown action: {}", id);
            }
        } else {
            log.error("Action ID is null (action not registered?)");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean enabled = false;

        if (file != null) {
            String ext = file.getExtension();
            if (ext != null) {
                ext = ext.toLowerCase();
                enabled = ext.equals("xml") || ext.equals("json") || ext.equals("yaml") || ext.equals("yml");
            }
        }

        e.getPresentation().setEnabledAndVisible(enabled);
    }

    private void convert(Project project, VirtualFile file, String targetLang) {
        try {
            PrismContext prismContext = StudioPrismContextService.getPrismContext(project);
            ParsingContext parsingCtx = prismContext.createParsingContextForCompatibilityMode();

            String code = VfsUtilCore.loadText(file);

            if (code.isEmpty()) {
                throw new Exception("Body input is empty.");
            }

            RootXNodeImpl root = (RootXNodeImpl) prismContext.parserFor(code)
                    .language(file.getFileType().getName().toLowerCase())
                    .context(parsingCtx)
                    .parseToXNode();

            String convertedCode = prismContext.serializerFor(targetLang).serialize(root);
            createFile(project, file.getParent(), file.getNameWithoutExtension() + "." + targetLang.toLowerCase(), convertedCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createFile(Project project, VirtualFile parentDir, String fileName, String content) {
        if (parentDir.findChild(fileName) == null) {
            WriteAction.computeAndWait(() -> {
                try {
                    VirtualFile newFile = parentDir.createChildData(null, fileName);
                    newFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                    return newFile;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } else {
            Notification notification = new Notification(
                    "midpointConverter",
                    "Midpoint converter",
                    "File " + fileName + " already exists.",
                    NotificationType.ERROR
            );
            Notifications.Bus.notify(notification, project);
        }
    }

}
