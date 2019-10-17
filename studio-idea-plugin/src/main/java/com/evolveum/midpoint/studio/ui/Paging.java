package com.evolveum.midpoint.studio.ui;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Paging {

    private int from = 0;

    private int to = 500;

    private int pageSize = 500;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Paging copy() {
        Paging p = new Paging();
        p.setFrom(from);
        p.setTo(to);
        p.setPageSize(pageSize);

        return p;
    }
}
