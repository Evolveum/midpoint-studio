package com.evolveum.midpoint.studio.ui;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointConsoleView extends ConsoleViewImpl implements AnsiEscapeDecoder.ColoredTextAcceptor {

    public MidPointConsoleView(Project project) {
        super(project, true);
    }

    @Override
    public void coloredTextAvailable(@NotNull String text, @NotNull Key attributes) {
        print(text, ConsoleViewContentType.getConsoleViewType(attributes));
    }
}
