package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public abstract class FileEditorBase<F extends VirtualFile> implements FileEditor {

    private final Project project;

    private final F file;

    private final UserDataHolderBase userDataHolder = new UserDataHolderBase();

    private final List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

    public FileEditorBase(@NotNull Project project, @NotNull F file) {
        this.project = project;
        this.file = file;
    }

    @Override
    public F getFile() {
        return file;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return userDataHolder.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userDataHolder.putUserData(key, value);
    }

}
