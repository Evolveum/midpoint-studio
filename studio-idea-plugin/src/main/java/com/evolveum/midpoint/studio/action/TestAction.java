package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    public TestAction() {
        super("MidPoint Test Action");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        if (!MidPointUtils.isVisibleWithMidPointFacet(e)) {
            e.getPresentation().setVisible(false);
            return;
        }

        boolean internal = ApplicationManager.getApplication().isInternal();
        e.getPresentation().setVisible(internal);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        Project project = evt.getProject();
        if (project == null) {
            return;
        }

//        DiffRequestFactory factory = DiffRequestFactory.getInstance();
//        VirtualFile file = VfsUtil.;
//        List<byte[]> contents = List.of(
//                "first1".getBytes(),
//                "".getBytes(),
//                "third3".getBytes()
//        );
//
//        try {
//            MergeRequest request = factory.createMergeRequest(project, file, contents, "my title", List.of("first", "second", "third"), c -> {
//                System.out.println("merge request created");
//            });
//
//            MergeRequestProcessor processor = new MergeRequestProcessor(project) {
//                @Override
//                public void closeDialog() {
//
//                }
//
//                @Override
//                protected @Nullable JRootPane getRootPane() {
//                    return new JRootPane();
//                }
//            };
//            processor.init(request);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
