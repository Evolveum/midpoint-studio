package com.evolveum.midpoint.studio.impl.ide;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointErrorReportSubmitter extends ErrorReportSubmitter {

    @NotNull
    @Override
    public String getReportActionText() {
        return "Report Error";
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull
            Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {

        // todo implement submit to jira
        return super.submit(events, additionalInfo, parentComponent, consumer);
    }
}
