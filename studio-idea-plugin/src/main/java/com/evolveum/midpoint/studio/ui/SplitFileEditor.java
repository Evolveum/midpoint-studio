package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class SplitFileEditor<L extends FileEditor, R extends FileEditor> extends UserDataHolderBase implements FileEditor {

    private SplitFileEditorState.SplitLayout splitLayout = SplitFileEditorState.SplitLayout.BOTH;

    private FileEditor leftEditor;

    private FileEditor rightEditor;

    private JComponent component;

    public SplitFileEditor(FileEditor leftEditor, FileEditor rightEditor) {
        this.leftEditor = leftEditor;
        this.rightEditor = rightEditor;

        initLayout();
    }

    private void initLayout() {
        OnePixelSplitter splitter = new OnePixelSplitter(false);
        splitter.setFirstComponent(leftEditor.getComponent());
        splitter.setSecondComponent(rightEditor.getComponent());

        JPanel root = new BorderLayoutPanel();
        root.add(splitter, BorderLayout.CENTER);

        component = root;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return component;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        if (leftEditor.getComponent().isVisible()) {
            return leftEditor.getPreferredFocusedComponent();
        }

        return rightEditor.getPreferredFocusedComponent();
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        if (!(state instanceof SplitFileEditorState)) {
            return;
        }

        SplitFileEditorState splitState = (SplitFileEditorState) state;
        if (splitState.getLeftState() != null) {
            leftEditor.setState(splitState.getLeftState());
        }
        if (splitState.getRightState() != null) {
            rightEditor.setState(splitState.getRightState());
        }
        if (splitState.getSplitLayout() != null) {
            splitLayout = SplitFileEditorState.SplitLayout.valueOf(splitState.getSplitLayout());
            updateLayout();
        }
    }

    private void updateLayout() {
        leftEditor.getComponent().setVisible(splitLayout.isShowEditor());
        rightEditor.getComponent().setVisible(splitLayout.isShowPreview());

        component.repaint();
    }

    @Override
    public boolean isModified() {
        return leftEditor.isModified() || rightEditor.isModified();
    }

    @Override
    public boolean isValid() {
        return leftEditor.isValid() || rightEditor.isValid();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // todo implement
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // todo implement
    }

    @Override
    public void dispose() {
        Disposer.dispose(leftEditor);
        Disposer.dispose(rightEditor);
    }

    public SplitFileEditorState.SplitLayout getSplitLayout() {
        return splitLayout;
    }

    public FileEditor getLeftEditor() {
        return leftEditor;
    }

    public FileEditor getRightEditor() {
        return rightEditor;
    }
}
