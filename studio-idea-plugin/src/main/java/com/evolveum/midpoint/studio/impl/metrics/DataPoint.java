package com.evolveum.midpoint.studio.impl.metrics;

import java.util.Date;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DataPoint {

    private int id;

    private Date timestamp;

    private Node node;

    private MetricsKey key;

    private double value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public MetricsKey getKey() {
        return key;
    }

    public void setKey(MetricsKey key) {
        this.key = key;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
