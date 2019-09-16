package com.evolveum.midpoint.studio.impl;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Event<T> {

    private String id;

    private T object;

    public Event(String id) {
        this(id, null);
    }

    public Event(String id, T object) {
        this.id = id;
        this.object = object;
    }

    public String getId() {
        return id;
    }

    public T getObject() {
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event<?> event = (Event<?>) o;

        if (id != null ? !id.equals(event.id) : event.id != null) return false;
        return object != null ? object.equals(event.object) : event.object == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", object=" + object +
                '}';
    }
}
