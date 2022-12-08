package com.evolveum.midpoint.studio.ui.profiler;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProfilingEditorState implements FileEditorState {

    @Override
    public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
        return false;
    }
}
