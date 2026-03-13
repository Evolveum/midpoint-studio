package com.evolveum.midpoint.studio.ui.dialog.wizard;

import com.evolveum.midpoint.studio.ui.dialog.wizard.navigation.NavigationItem;

import javax.swing.*;
import java.util.*;

public class NavigationStep {

    private String title;
    private StepStatus status;
    private List<NavigationStep> children = new ArrayList<>();

    private NavigationItem navigationItem;
    private JPanel contentPanel;

    public NavigationStep(String title, StepStatus status, JPanel contentPanel) {
        this.title = title;
        this.status = status;
        this.contentPanel = contentPanel;
    }

    public NavigationStep(String title, StepStatus status, Runnable action) {
        this.title = title;
        this.status = status;
    }

    public void addChild(NavigationStep step) {
        children.add(step);
    }

    public String getTitle() { return title; }

    public StepStatus getStatus() { return status; }

    public List<NavigationStep> getChildren() { return children; }

    public boolean isGroup() {
        return !children.isEmpty();
    }

    public void setNavigationItem(NavigationItem navigationItem) {
        this.navigationItem = navigationItem;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setContentPanel(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public NavigationStep getPreviousStep() {
        var flattened = flatten();
        int currentIndex = flattened.indexOf(this);

        if (currentIndex > 0) {
            return flattened.get(currentIndex - 1);
        }

        return null;
    }

    public NavigationStep getNextStep(NavigationStep rootStep) {
        var flattened = flatten(rootStep);
        int currentIndex = flattened.indexOf(this);

        if (currentIndex != -1 && currentIndex < flattened.size() - 1) {
            return flattened.get(currentIndex + 1);
        }

        return null;
    }

    private List<NavigationStep> flatten(NavigationStep root) {
        List<NavigationStep> result = new ArrayList<>();
        Queue<NavigationStep> queue = new ArrayDeque<>();

        queue.add(root);

        while (!queue.isEmpty()) {
            NavigationStep node = queue.poll();
            result.add(node);
            queue.addAll(node.children);
        }

        return result;
    }

    public void printDump(String prefix) {
        System.out.println(prefix + "- " + title + " [" + status + "]");

        for (NavigationStep child : getChildren()) {
            child.printDump(prefix + "  ");
        }
    }
}
