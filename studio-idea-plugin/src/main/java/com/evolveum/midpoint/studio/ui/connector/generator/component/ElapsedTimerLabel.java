package com.evolveum.midpoint.studio.ui.connector.generator.component;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.Timer;

public class ElapsedTimerLabel extends JBLabel {

    private static final String FORMAT = "Elapsed time: %dm %ds";

    private long elapsedSeconds;
    private final Timer timer;

    public ElapsedTimerLabel(long initialSeconds) {
        super("", SwingConstants.CENTER);

        this.elapsedSeconds = initialSeconds;
        setForeground(UIUtil.getInactiveTextColor());
        updateText();

        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            updateText();
        });
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    private void updateText() {

        setText(FORMAT.formatted(
                elapsedSeconds / 60,
                elapsedSeconds % 60
        ));
    }
}
