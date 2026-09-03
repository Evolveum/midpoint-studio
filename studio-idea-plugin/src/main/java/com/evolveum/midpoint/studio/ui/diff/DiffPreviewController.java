package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.Side;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class DiffPreviewController {

    private final Project project;
    private final PsiFile originalPsiFile;

    private DialogWrapper dialog;
    private DiffRequestPanel diffPanel;

    public DiffPreviewController(Project project, PsiFile originalPsiFile) {
        this.project = project;
        this.originalPsiFile = originalPsiFile;
    }

    public void show(String newContent) {
        if (dialog == null) {
            dialog = new DialogWrapper(project) {
                {
                    setTitle("Live Diff Preview");
                    init();
                }

                @Override
                protected JComponent createCenterPanel() {
                    JPanel panel = new JPanel(new BorderLayout());

                    diffPanel = DiffManager.getInstance()
                            .createRequestPanel(project, getDisposable(), null);

                    panel.add(diffPanel.getComponent(), BorderLayout.CENTER);
                    return panel;
                }
            };

            dialog.show();
        }

        updateDiff(newContent);
    }

//    private void showDiffRequest(@NotNull PsiFile originalPsiFile, @NotNull String updated) {
//        SimpleDiffRequest diffRequest = new SimpleDiffRequest(
//                "Preview Changes at Smart Suggestion",
//                DiffContentFactory.getInstance().create(
//                        project,
//                        originalPsiFile.getFileDocument()
//                ),
//                DiffContentFactory.getInstance().create(
//                        project,
//                        updated,
//                        originalPsiFile.getFileType()
//                ),
//                "Original",
//                "Suggested"
//        );
//
//        AnAction acceptAction = new AnAction(
//                "Accept All Suggestions",
//                "Accept all generated smart suggestions for this resource",
//                AllIcons.Actions.ShowWriteAccess
//        ) {
//            @Override
//            public void actionPerformed(@NotNull AnActionEvent e) {
//                Project project = e.getProject();
//                if (project == null) return;
//
//                Document document = originalPsiFile.getViewProvider().getDocument();
//                if (document == null) return;
//
//                WriteCommandAction.runWriteCommandAction(project, () ->
//                        document.setText(updated)
//                );
//
//                VirtualFile file = originalPsiFile.getVirtualFile();
//                if (file != null) {
//                    FileEditorManager.getInstance(project)
//                            .openFile(file, true);
//                }
//
//                for (VirtualFile openFile :
//                        FileEditorManager.getInstance(project).getOpenFiles()) {
//                    String name = openFile.getName();
//                    if (name.contains("Preview Changes at Smart Suggestion")) {
//                        FileEditorManager.getInstance(project).closeFile(openFile);
//                    }
//                }
//            }
//        };
//
//        diffRequest.putUserData(
//                DiffUserDataKeys.CONTEXT_ACTIONS,
//                List.of(acceptAction)
//        );
//
//        scrollToFirstNewBlock(originalPsiFile.getText(), updated, diffRequest);
//
//        DiffManager.getInstance().showDiff(project, diffRequest);
//    }

    public void updateDiff(String newContent) {
        diffPanel.setRequest(new SimpleDiffRequest(
                "Preview Changes at Smart Suggestion",
                DiffContentFactory.getInstance().create(
                        project,
                        originalPsiFile.getFileDocument()
                ),
                DiffContentFactory.getInstance().create(
                        project,
                        newContent,
                        originalPsiFile.getFileType()
                ),
                "Original",
                "Suggested"
        ));
    }

    private void scrollToFirstNewBlock(
            @NotNull String original,
            @NotNull String updated,
            @NotNull SimpleDiffRequest diffRequest
    ) {
        List<LineFragment> fragments = ComparisonManager.getInstance()
                .compareLines(
                        original,
                        updated,
                        ComparisonPolicy.DEFAULT,
                        new EmptyProgressIndicator()
                );

        fragments.stream()
                .filter(fragment ->
                        fragment.getStartLine1() == fragment.getEndLine1()
                                && fragment.getStartLine2() < fragment.getEndLine2()
                )
                .min(Comparator.comparingInt(LineFragment::getStartLine2))
                .ifPresent(fragment ->
                        diffRequest.putUserData(
                                DiffUserDataKeys.SCROLL_TO_LINE,
                                Pair.create(Side.RIGHT, fragment.getStartLine2())
                        )
                );
    }
}
