package com.evolveum.midpoint.studio.ui.dialog.wizard;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WizardStep<CT> {

    private final String title;
    private final WizardStepStatus status;
    private final List<WizardStep<CT>> children = new ArrayList<>();
    private WizardContent contentPanel;

    public WizardStep(String title, WizardStepStatus status, @NotNull WizardContent contentPanel) {
        this.title = title;
        this.status = status;
        this.contentPanel = contentPanel;
    }

    public void addChild(WizardStep<CT> step) {
        children.add(step);
    }

    public String getTitle() { return title; }

    public WizardStepStatus getStatus() { return status; }

    public List<WizardStep<CT>> getChildren() { return children; }

    public boolean isGroup() {
        return !children.isEmpty();
    }

    public @NotNull WizardContent getContentPanel() {
        return contentPanel;
    }

    public void setContentPanel(WizardContent contentPanel) {
        this.contentPanel = contentPanel;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public WizardStep<CT> getPreviousStep(WizardStep<CT> rootStep) {
        var flattened = flatten(List.of(rootStep));
        int currentIndex = flattened.indexOf(this);

        if (currentIndex > 0) {
            return flattened.get(currentIndex - 1);
        }

        return null;
    }

    public WizardStep<CT> getNextStep(WizardStep<CT> rootStep) {
        var flattened = flatten(List.of(rootStep));
        int currentIndex = flattened.indexOf(this);

        if (currentIndex != -1 && currentIndex < flattened.size() - 1) {
            return flattened.get(currentIndex + 1);
        }

        return null;
    }

    private void traverse(WizardStep<CT> node, List<WizardStep<CT>> result) {
        if (node == null) return;

        result.add(node);

        for (WizardStep<CT> child : node.children) {
            traverse(child, result);
        }
    }

    public List<WizardStep<CT>> flatten(List<WizardStep<CT>> roots) {
        List<WizardStep<CT>> result = new ArrayList<>();
        for (WizardStep<CT> root : roots) {
            traverse(root, result);
        }
        return result;
    }

    private List<WizardStep<CT>> flatten(WizardStep<CT> rootStep) {
        List<WizardStep<CT>> result = new ArrayList<>();
        Queue<WizardStep<CT>> queue = new ArrayDeque<>();

        queue.add(rootStep);

        while (!queue.isEmpty()) {
            WizardStep<CT> node = queue.poll();
            result.add(node);
            queue.addAll(node.children);
        }

        return result;
    }

    public void printDump(String prefix) {
        System.out.println(prefix + "- " + title + " [" + status + "]");

        for (WizardStep<CT> child : getChildren()) {
            child.printDump(prefix + "  ");
        }
    }
}
