package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.RestObjectManager;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.IdeBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(TestAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "daf", false) {
            public void run(ProgressIndicator indicator) {
                indicator.setText("This is how you update the indicator");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
                indicator.setFraction(0.5);  // halfway done
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
            }
        });
        Messages.showMessageDialog(project, "Hello world!", "Greeting", Messages.getInformationIcon());

//        VirtualFile[] data = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
//        if (data == null || data.length == 0 || e.getProject() == null) {
//            return;
//        }
//
//        try {
//            RestObjectManager manager = RestObjectManager.getInstance(e.getProject());
//            List list = manager.parseObjects(data[0]);
//
//            LOG.info("Parsed: " + list);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

//        Editor editor = e.getData(PlatformDataKeys.EDITOR);
//        if (editor != null) {
//            HintManager hintManager = HintManager.getInstance();
//            hintManager.showErrorHint(editor, "John Doe made a mistake!");
//        }



////        ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("MidPoint");
////        System.out.println(window);
//
////        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
////        System.out.println(files);
////
//        Notification notification = new Notification(IdeBundle.message("low.memory.notification.title"),
//                "Test notification",
//                "John Doe test notification content",
//                NotificationType.WARNING);
//
//        notification.addAction(new NotificationAction("Do something about it") {
//
//            @Override
//            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
//                System.out.println("aaaaa");
//            }
//        });
//        Notifications.Bus.notify(notification);
//
////        showDialog(e.getProject());
    }


    @Override
    public boolean startInTransaction() {
        return true;
    }

//    @Nullable
//    private EnvironmentListDialog showDialog(Project project) {
//        final Ref<EnvironmentListDialog> dialog = Ref.create();
//
//        ApplicationManager.getApplication().invokeAndWait(() -> {
//            dialog.set(new EnvironmentListDialog(project));
//            dialog.get().show();
//        }, ModalityState.any());
//
//        return dialog.get();
//    }
}
