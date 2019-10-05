package com.evolveum.midpoint.studio.ui.profiler;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProfilingEditorState implements FileEditorState {

    @Override
    public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
        return false;
    }
}
