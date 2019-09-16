package com.evolveum.midpoint.studio.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

//        ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("MidPoint");
//        System.out.println(window);

//        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
//        System.out.println(files);
//
//        Notification notification = new Notification(IdeBundle.message("low.memory.notification.title"),
//                IdeBundle.message("low.memory.notification.title"),
//                IdeBundle.message("low.memory.notification.content"),
//                NotificationType.WARNING);
//        notification.addAction(new NotificationAction(IdeBundle.message("low.memory.notification.action")) {
//            @Override
//            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
//                System.out.println("aaaaa");
//            }
//        });
//        Notifications.Bus.notify(notification);

//        showDialog(e.getProject());
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
