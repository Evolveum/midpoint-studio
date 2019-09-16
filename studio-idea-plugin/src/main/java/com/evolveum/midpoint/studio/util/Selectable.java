package com.evolveum.midpoint.studio.util;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Selectable<T> {

    private T object;

    private boolean selected;

    public Selectable(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Selectable<?> that = (Selectable<?>) o;

        if (selected != that.selected) return false;
        return object != null ? object.equals(that.object) : that.object == null;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + (selected ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Selectable{" +
                "s=" + selected +
                ", o=" + object +
                '}';
    }
}
