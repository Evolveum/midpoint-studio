package com.evolveum.midpoint.studio.util;

import javax.swing.*;

/**
 * @deprecated only needed for compatibility with 191.*
 *
 * Created by Viliam Repan (lazyman).
 */
@Deprecated
public class ExtendedListSelectionModel extends DefaultListSelectionModel {

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
