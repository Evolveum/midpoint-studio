package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class SplitFileEditorProvider implements AsyncFileEditorProvider, DumbAware {

    private static final String LEFT_EDITOR = "left-editor";
    private static final String RIGHT_EDITOR = "right-editor";
    private static final String SPLIT_LAYOUT = "split_layout";

    @NotNull
    private FileEditorProvider leftProvider;
    @NotNull
    private FileEditorProvider rightProvider;

    private String editorTypeId;

    public SplitFileEditorProvider(@NotNull FileEditorProvider leftProvider, @NotNull FileEditorProvider rightProvider) {
        this.leftProvider = leftProvider;
        this.rightProvider = rightProvider;

        editorTypeId = "split-provider[" + this.leftProvider.getEditorTypeId() + "," + this.rightProvider.getEditorTypeId() + "]";
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return leftProvider.accept(project, file) && rightProvider.accept(project, file);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return createEditorAsync(project, file).build();
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return editorTypeId;
    }

    @NotNull
    @Override
    public Builder createEditorAsync(@NotNull final Project project, @NotNull final VirtualFile file) {
        Builder firstBuilder = getBuilderFromEditorProvider(leftProvider, project, file);
        Builder secondBuilder = getBuilderFromEditorProvider(rightProvider, project, file);

        return new Builder() {

            @Override
            public FileEditor build() {
                return createSplitEditor(firstBuilder.build(), secondBuilder.build());
            }
        };
    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element source, @NotNull Project project, @NotNull VirtualFile file) {
        Element child = source.getChild(LEFT_EDITOR);

        FileEditorState leftState = null;
        if (child != null) {
            leftState = leftProvider.readState(child, project, file);
        }

        child = source.getChild(RIGHT_EDITOR);

        FileEditorState rightState = null;
        if (child != null) {
            rightState = rightProvider.readState(child, project, file);
        }

        Attribute attribute = source.getAttribute(SPLIT_LAYOUT);

        String layoutName = attribute != null ? attribute.getValue() : null;

        return new SplitFileEditorState(layoutName, leftState, rightState);
    }

    @Override
    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        if (!(state instanceof SplitFileEditorState)) {
            return;
        }
        final SplitFileEditorState compositeState = (SplitFileEditorState) state;

        Element child = new Element(LEFT_EDITOR);
        if (compositeState.getLeftState() != null) {
            leftProvider.writeState(compositeState.getLeftState(), project, child);
            targetElement.addContent(child);
        }

        child = new Element(RIGHT_EDITOR);
        if (compositeState.getRightState() != null) {
            rightProvider.writeState(compositeState.getRightState(), project, child);
            targetElement.addContent(child);
        }

        if (compositeState.getSplitLayout() != null) {
            targetElement.setAttribute(SPLIT_LAYOUT, compositeState.getSplitLayout());
        }
    }

    protected abstract FileEditor createSplitEditor(@NotNull FileEditor firstEditor, @NotNull FileEditor secondEditor);

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @NotNull
    private static Builder getBuilderFromEditorProvider(@NotNull final FileEditorProvider provider,
                                                        @NotNull final Project project,
                                                        @NotNull final VirtualFile file) {
        if (provider instanceof AsyncFileEditorProvider) {
            return ((AsyncFileEditorProvider) provider).createEditorAsync(project, file);
        }

        return new Builder() {

            @Override
            public FileEditor build() {
                return provider.createEditor(project, file);
            }
        };
    }
}
