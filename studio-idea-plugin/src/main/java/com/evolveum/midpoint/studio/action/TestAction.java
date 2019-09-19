package com.evolveum.midpoint.studio.action;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.IdeBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            HintManager hintManager = HintManager.getInstance();
            hintManager.showErrorHint(editor, "John Doe made a mistake!");
        }



//        ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("MidPoint");
//        System.out.println(window);

//        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
//        System.out.println(files);
//
        Notification notification = new Notification(IdeBundle.message("low.memory.notification.title"),
                "Test notification",
                "John Doe test notification content",
                NotificationType.WARNING);

        notification.addAction(new NotificationAction("Do something about it") {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                System.out.println("aaaaa");
            }
        });
        Notifications.Bus.notify(notification);

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
