package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.notebooks.visualization.r.inlays.components.EmptyComponentPanel;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class DiffProcessor<O extends ObjectType> {

    private final Project project;

    private final String leftName;
    private final VirtualFile leftFile;

    private final String rightName;
    private final VirtualFile rightFile;

    private PrismObject<O> leftObject;
    private PrismObject<O> rightObject;

    private ObjectDelta<O> delta;

    private DiffPanel panel;

    public DiffProcessor(
            @NotNull Project project,
            @NotNull String leftName,
            @NotNull VirtualFile leftFile,
            @NotNull String rightName,
            @NotNull VirtualFile rightFile) {

        this.project = project;

        this.leftName = leftName;
        this.leftFile = leftFile;

        this.rightName = rightName;
        this.rightFile = rightFile;
    }

    public <O extends ObjectType> void initialize() {
        try {
            leftObject = parseObject(leftFile);
            rightObject = parseObject(rightFile);

            delta = leftObject.diff(rightObject, EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS_NATURAL_KEYS);

            panel = new DiffPanel(project, delta);
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't parse object", ex);
        }
    }

    private <O extends ObjectType> PrismObject<O> parseObject(VirtualFile file) throws SchemaException, IOException {
        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        try (InputStream is = file.getInputStream()) {
            PrismParser parser = ClientUtils.createParser(ctx, is);
            return parser.parse();
        }
    }

    public String getName() {
        return "aaa1";
    }

    public JComponent getComponent() {
        return panel;
    }
}
