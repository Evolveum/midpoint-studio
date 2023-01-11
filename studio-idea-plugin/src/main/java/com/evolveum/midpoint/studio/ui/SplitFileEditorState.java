package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SplitFileEditorState implements FileEditorState {
    @Nullable
    private final String splitLayout;
    @Nullable
    private final FileEditorState leftState;
    @Nullable
    private final FileEditorState rightState;

    public SplitFileEditorState(@Nullable String splitLayout, @Nullable FileEditorState firstState, @Nullable FileEditorState secondState) {
        this.splitLayout = splitLayout;
        leftState = firstState;
        rightState = secondState;
    }

    @Nullable
    public String getSplitLayout() {
        return splitLayout;
    }

    @Nullable
    public FileEditorState getLeftState() {
        return leftState;
    }

    @Nullable
    public FileEditorState getRightState() {
        return rightState;
    }

    @Override
    public boolean canBeMergedWith(@NotNull FileEditorState other, @NotNull FileEditorStateLevel level) {
        if (!(other instanceof SplitFileEditorState)) {
            return false;
        }

        return (leftState == null || leftState.canBeMergedWith(((SplitFileEditorState) other).leftState, level))
                && (rightState == null || rightState.canBeMergedWith(((SplitFileEditorState) other).rightState, level));
    }

    public enum SplitLayout {

        EDITOR(true, false, "Editor only"),

        PREVIEW(false, true, "Preview only"),

        BOTH(true, true, "Editor and preview");

        private final boolean showEditor;

        private final boolean showPreview;

        private final String label;

        SplitLayout(boolean showEditor, boolean showPreview, String label) {
            this.showEditor = showEditor;
            this.showPreview = showPreview;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public boolean isShowEditor() {
            return showEditor;
        }

        public boolean isShowPreview() {
            return showPreview;
        }

        public String getLabel() {
            return label;
        }
    }
}
