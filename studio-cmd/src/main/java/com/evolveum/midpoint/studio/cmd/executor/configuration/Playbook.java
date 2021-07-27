package com.evolveum.midpoint.studio.cmd.executor.configuration;

import com.evolveum.midpoint.studio.cmd.executor.configuration.task.TaskTask;
import com.evolveum.midpoint.studio.cmd.executor.configuration.task.UploadTask;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Playbook extends ConfigurationObject {

    private Midpoint midpoint;

    private Map<String, Object> variables;

    private List<Task> tasks;

    public Midpoint getMidpoint() {
        return midpoint;
    }

    public void setMidpoint(Midpoint midpoint) {
        this.midpoint = midpoint;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(value = UploadTask.class),
            @JsonSubTypes.Type(value = TaskTask.class)
    })
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
