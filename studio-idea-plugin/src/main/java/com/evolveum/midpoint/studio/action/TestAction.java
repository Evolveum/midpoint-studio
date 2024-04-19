package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.ui.diff.DiffProcessor;
import com.evolveum.midpoint.studio.ui.diff.DiffSource;
import com.evolveum.midpoint.studio.ui.diff.DiffSourceType;
import com.evolveum.midpoint.studio.ui.diff.DiffVirtualFile;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    public TestAction() {
        super("MidPoint Test Action");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        if (!MidPointUtils.isVisibleWithMidPointFacet(e)) {
            e.getPresentation().setVisible(false);
            return;
        }

        boolean visible = MidPointUtils.isDevelopmentMode(true);
        e.getPresentation().setVisible(visible);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        Project project = evt.getProject();
        if (project == null) {
            return;
        }

        VirtualFile parent = project.getBaseDir();

        VirtualFile previousInitialFile = parent.findFileByRelativePath("./_3-merge/previous-initial-system.xml");
        VirtualFile currentInitialFile = parent.findFileByRelativePath("./_3-merge/current-initial-system.xml");
        VirtualFile currentObjectFile = parent.findFileByRelativePath("./_3-merge/current-system.xml");


        try {
            PrismObject<?> previousInitial = parse(previousInitialFile);
            PrismObject<?> currentInitial = parse(currentInitialFile);
            PrismObject<?> currentObject = parse(currentObjectFile);
//
//            InitialObjectDiffProcessor processor = new InitialObjectDiffProcessor(
//                    project, previousInitial, currentInitial, currentObject);
//
//            ThreeWayMergeVirtualFile file = new ThreeWayMergeVirtualFile(processor);
//            MidPointUtils.openFile(project, file);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

            DiffSource left = new DiffSource("System configuration", currentObjectFile, DiffSourceType.LOCAL);
            DiffSource right = new DiffSource("System configuration", previousInitialFile, DiffSourceType.REMOTE);

            DiffProcessor processor = new DiffProcessor(project, left, right);
            processor.initialize();
            DiffVirtualFile file = new DiffVirtualFile(processor);

            MidPointUtils.openFile(project, file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private PrismObject<?> parse(VirtualFile file) throws Exception {
        return ClientUtils.createParser(
                        MidPointUtils.DEFAULT_PRISM_CONTEXT,
                        new ByteArrayInputStream(file.contentsToByteArray()))
                .parse();
    }

    private static abstract class Dialog extends DialogWrapper {

        public Dialog(@Nullable Project project) {
            super(project, false);

            setSize(800, 600);
            init();
        }
    }
}
