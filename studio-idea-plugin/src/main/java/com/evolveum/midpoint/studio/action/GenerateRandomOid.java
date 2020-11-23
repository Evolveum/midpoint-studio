package com.evolveum.midpoint.studio.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.StatusBarEx;
import com.intellij.util.ui.UIUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.UUID;

/**
 * Created by lazyman on 10/02/2017.
 */
public class GenerateRandomOid extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        UUID uuid = UUID.randomUUID();
        String oid = uuid.toString();

        StringSelection selection = new StringSelection(oid);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        UIUtil.invokeLaterIfNeeded(() -> {
            IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(e.getProject());
            if (ideFrame != null) {
                ((StatusBarEx) ideFrame.getStatusBar()).notifyProgressByBalloon(MessageType.INFO, "Oid copied to system clipboard.");
            }
        });
    }
}
