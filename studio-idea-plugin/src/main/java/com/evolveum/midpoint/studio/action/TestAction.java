package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.schema.delta.ThreeWayMergeOperation;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.StudioAction;
import com.evolveum.midpoint.studio.ui.diff.ThreeWayMergeTree;
import com.evolveum.midpoint.studio.ui.diff.ThreeWayMergeTreeModel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.ByteArrayInputStream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends StudioAction {

    @Override
    protected boolean isVisible(@NotNull AnActionEvent e) {
        if (!super.isVisible(e)) {
            return false;
        }

        return MidPointUtils.isDevelopmentMode(true);
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
            PrismObject previousInitial = parse(project, previousInitialFile);
            PrismObject currentInitial = parse(project, currentInitialFile);
            PrismObject currentObject = parse(project, currentObjectFile);

            ThreeWayMergeOperation operation = new ThreeWayMergeOperation(
                    currentInitial, currentObject, previousInitial, EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS_NATURAL_KEYS);

            ApplicationManager.getApplication().invokeAndWait(() -> {
                ThreeWayMergeTreeModel model = new ThreeWayMergeTreeModel();
                model.setData(operation);
                JComponent panel = new ThreeWayMergeTree(model);
                DialogBuilder db = new DialogBuilder();
                db.centerPanel(ScrollPaneFactory.createScrollPane(panel));

                db.showAndGet();
            });


//            DiffSource left = new DiffSource("System configuration", DiffSourceType.LOCAL, currentObject);
//            DiffSource right = new DiffSource("System configuration", DiffSourceType.REMOTE, previousInitial);
//
//            DiffProcessor processor = new DiffProcessor(project, left, right);
//            processor.computeDelta();
//            DiffVirtualFile file = new DiffVirtualFile(processor);
//
//            MidPointUtils.openFile(project, file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private PrismObject<?> parse(Project project, VirtualFile file) throws Exception {
        return ClientUtils.createParser(
                        StudioPrismContextService.getPrismContext(project),
                        new ByteArrayInputStream(file.contentsToByteArray()))
                .parse();
    }
}
