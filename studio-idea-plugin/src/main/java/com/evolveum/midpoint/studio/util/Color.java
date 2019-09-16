package com.evolveum.midpoint.studio.util;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Color implements Serializable {

    private int red;
    private int green;
    private int blue;

    public Color() {
    }

    public Color(java.awt.Color color) {
        if (color == null) {
            color = java.awt.Color.WHITE;
        }

        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public java.awt.Color asAwtColor() {
        return new java.awt.Color(red, green, blue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        if (red != color.red) return false;
        if (green != color.green) return false;
        return blue == color.blue;
    }

    @Override
    public int hashCode() {
        int result = red;
        result = 31 * result + green;
        result = 31 * result + blue;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Color[");
        sb.append(red);
        sb.append(",").append(green);
        sb.append(",").append(blue);
        sb.append(']');
        return sb.toString();
    }
}
