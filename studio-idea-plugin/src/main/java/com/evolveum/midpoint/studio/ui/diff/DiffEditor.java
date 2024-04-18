package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class DiffEditor implements FileEditor {

    private final Project project;

    private final DiffVirtualFile file;

    private final UserDataHolderBase userDataHolder = new UserDataHolderBase();

    private final List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

    public DiffEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.file = (DiffVirtualFile) file;
        this.project = project;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    @Override
    public @NotNull JComponent getComponent() {
        return file.getProcessor().getComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return file.getProcessor().getName();
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        // todo implement
    }

    @Override
    public boolean isModified() {
        // todo implement
        return false;
    }

    @Override
    public boolean isValid() {
        // todo implement
        return true;
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    @Override
    public void dispose() {
        // todo implement
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return userDataHolder.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userDataHolder.putUserData(key, value);
    }

    @Override
    public VirtualFile getFile() {
        return file;
    }
}
