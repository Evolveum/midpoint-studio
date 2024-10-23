package com.evolveum.midpoint.studio.action.task;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskState {

    private int processedFile;

    private int skippedFile;

    private int processed;

    private int skipped;

    private int failed;

    private boolean stoppedOnError;

    public void incrementProcessedFile() {
        processedFile++;
    }

    public void incrementSkippedFile() {
        skippedFile++;
    }

    public void incrementProcessed() {
        processed++;
    }

    public void incrementProcessed(int add) {
        processed += add;
    }

    public void incrementSkipped() {
        skipped++;
    }

    public void incrementFailed() {
        failed++;
    }

    public void setStopOnError() {
        this.stoppedOnError = true;
    }

    public int getProcessedFile() {
        return processedFile;
    }

    public int getSkippedFile() {
        return skippedFile;
    }

    public int getProcessed() {
        return processed;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getFailed() {
        return failed;
    }

    public boolean isStoppedOnError() {
        return stoppedOnError;
    }
}
