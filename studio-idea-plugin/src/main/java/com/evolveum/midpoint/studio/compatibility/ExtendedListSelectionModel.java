package com.evolveum.midpoint.studio.compatibility;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/**
 * @deprecated only needed for compatibility with 191.*
 *
 * Created by Viliam Repan (lazyman).
 */
@Deprecated
public class ExtendedListSelectionModel implements ListSelectionModel {

    private ListSelectionModel model;

    public ExtendedListSelectionModel(ListSelectionModel model) {
        this.model = model;
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        model.setSelectionInterval(index0, index1);
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        model.addSelectionInterval(index0, index1);
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
        model.removeSelectionInterval(index0, index1);
    }

    @Override
    public int getMinSelectionIndex() {
        return model.getMinSelectionIndex();
    }

    @Override
    public int getMaxSelectionIndex() {
        return model.getMaxSelectionIndex();
    }

    @Override
    public boolean isSelectedIndex(int index) {
        return model.isSelectedIndex(index);
    }

    @Override
    public int getAnchorSelectionIndex() {
        return model.getAnchorSelectionIndex();
    }

    @Override
    public void setAnchorSelectionIndex(int index) {
        model.setAnchorSelectionIndex(index);
    }

    @Override
    public int getLeadSelectionIndex() {
        return model.getLeadSelectionIndex();
    }

    @Override
    public void setLeadSelectionIndex(int index) {
        model.setLeadSelectionIndex(index);
    }

    @Override
    public void clearSelection() {
        model.clearSelection();
    }

    @Override
    public boolean isSelectionEmpty() {
        return model.isSelectionEmpty();
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        model.insertIndexInterval(index, length, before);
    }

    @Override
    public void removeIndexInterval(int index0, int index1) {
        model.removeIndexInterval(index0, index1);
    }

    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        model.setValueIsAdjusting(valueIsAdjusting);
    }

    @Override
    public boolean getValueIsAdjusting() {
        return model.getValueIsAdjusting();
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        model.setSelectionMode(selectionMode);
    }

    @Override
    public int getSelectionMode() {
        return model.getSelectionMode();
    }

    @Override
    public void addListSelectionListener(ListSelectionListener x) {
        model.addListSelectionListener(x);
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener x) {
        model.removeListSelectionListener(x);
    }

    public int[] getSelectedIndices() {
        int iMin = getMinSelectionIndex();
        int iMax = getMaxSelectionIndex();

        if ((iMin < 0) || (iMax < 0)) {
            return new int[0];
        }

        int[] rvTmp = new int[1+ (iMax - iMin)];
        int n = 0;
        for(int i = iMin; i <= iMax; i++) {
            if (isSelectedIndex(i)) {
                rvTmp[n++] = i;
            }
        }
        int[] rv = new int[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }

    public int getSelectedItemsCount() {
        int iMin = getMinSelectionIndex();
        int iMax = getMaxSelectionIndex();
        int count = 0;

        for(int i = iMin; i <= iMax; i++) {
            if (isSelectedIndex(i)) {
                count++;
            }
        }
        return count;
    }
}
