package com.evolveum.midpoint.studio.ui.metrics;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;

import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsEditorState implements FileEditorState {

    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
        return false;
    }
}
