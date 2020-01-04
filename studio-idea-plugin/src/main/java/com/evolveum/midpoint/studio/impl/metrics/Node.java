package com.evolveum.midpoint.studio.impl.metrics;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Node {

    private int id;

    private String oid;

    private String name;

    public Node(int id, String oid, String name) {
        this.id = id;
        this.oid = oid;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
