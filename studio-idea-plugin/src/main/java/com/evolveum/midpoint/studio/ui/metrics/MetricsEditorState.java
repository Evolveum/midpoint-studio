package com.evolveum.midpoint.studio.ui.metrics;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsEditorState implements FileEditorState {

    @Override
    public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
        return false;
    }
}
