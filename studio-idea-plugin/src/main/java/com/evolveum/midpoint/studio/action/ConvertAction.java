package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.converter.ConverterPanel;
import com.evolveum.midpoint.studio.util.LanguageUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Dominik.
 */
public class ConvertAction extends AnAction {

    private static final Logger log = Logger.getInstance(ConvertAction.class);

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String id = ActionManager.getInstance().getId(this);

        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || file == null) {
            return;
        }

        if (id == null) {
            log.error("Action ID is null (action not registered?)");
            return;
        }

        Language targetLanguage = switch (id) {
            case "MidPoint.Action.Converter.ConvertToXml" -> XMLLanguage.INSTANCE;
            case "MidPoint.Action.Converter.ConvertToJson" -> JsonLanguage.INSTANCE;
            case "MidPoint.Action.Converter.ConvertToYaml" -> YAMLLanguage.INSTANCE;
            default -> null;
        };

        if (targetLanguage == null) {
            log.error("Unknown action: " + id);
            return;
        }

        convert(project, file, targetLanguage);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean enabled = false;

        if (file != null) {
            String ext = file.getExtension();
            if (ext != null) {
                ext = ext.toLowerCase();
                enabled =
                        ext.equals(XMLLanguage.INSTANCE.getID().toLowerCase()) ||
                        ext.equals(JsonLanguage.INSTANCE.getID().toLowerCase()) ||
                        ext.equals(YAMLLanguage.INSTANCE.getID().toLowerCase()) ||
                        ext.equals("yml");
            }
        }

        e.getPresentation().setEnabledAndVisible(enabled);
    }

    private void convert(@NotNull Project project, @NotNull VirtualFile file, @NotNull Language targetLanguage) {
        String targetLang = LanguageUtils.toPrismLanguage(targetLanguage);
        if (targetLang == null) {
            notifyError(project, "Unsupported target language: " + targetLanguage.getID());
            return;
        }

        String sourceLang = file.getFileType() instanceof LanguageFileType fileType
                ? LanguageUtils.toPrismLanguage(fileType.getLanguage()) : null;
        if (sourceLang == null) {
            notifyError(project, "File " + file.getName() + " is not recognized as XML, JSON or YAML.");
            return;
        }

        try {
            String code = VfsUtilCore.loadText(file);
            if (code.isBlank()) {
                notifyError(project, "File " + file.getName() + " is empty.");
                return;
            }

            PrismContext prismContext = StudioPrismContextService.getPrismContext(project);
            ParsingContext parsingCtx = prismContext.createParsingContextForCompatibilityMode();

            RootXNode root = prismContext.parserFor(code)
                    .language(sourceLang)
                    .context(parsingCtx)
                    .parseToXNode();

            String convertedCode = prismContext.serializerFor(targetLang).serialize(root);
            createFile(project, file.getParent(), file.getNameWithoutExtension() + "." + targetLang, convertedCode);
        } catch (Exception ex) {
            log.warn("Couldn't convert " + file.getName() + " to " + targetLang, ex);
            notifyError(project, "Couldn't convert " + file.getName() + ": " + ex.getMessage());
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
            notifyError(project, "File " + fileName + " already exists.");
        }
    }

    private void notifyError(Project project, String message) {
        MidPointUtils.publishNotification(
                project, ConverterPanel.NOTIFICATION_KEY, "MidPoint converter", message, NotificationType.ERROR);
    }
}
