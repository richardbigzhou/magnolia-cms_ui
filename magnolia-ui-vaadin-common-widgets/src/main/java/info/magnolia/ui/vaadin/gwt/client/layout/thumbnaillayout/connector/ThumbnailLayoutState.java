/**
 * This file Copyright (c) 2010-2015 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector;

import info.magnolia.ui.vaadin.gwt.shared.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

/**
 * ThumbnailLayoutState.
 */
public class ThumbnailLayoutState extends AbstractComponentState {

    public int thumbnailAmount = 0;

    public int offset = 0;

    public float scaleRatio = -1;

    public ThumbnailSize size = new ThumbnailSize();

    public SelectionModel selection = new SelectionModel();

    /**
     * Is {@code true} only during the first request.
     */
    public boolean isFirstUpdate = true;

    /**
     * ThumbnailSize.
     */
    public static class ThumbnailSize implements Serializable {

        public int width = 0;

        public int height = 0;
    }

    /**
     * Selection model.
     */
    public static class SelectionModel implements Serializable {

        public List<Integer> selectedIndices = new ArrayList<>();

        public int min = -1;

        public int max = -1;

        public void toggleSelection(int index) {
            int indexToSelect = index;
            if (selectedIndices.size() == 1 && selectedIndices.contains(index)) {
                indexToSelect = -1;
            }

            selectedIndices.clear();

            if (indexToSelect >= 0) {
                selectedIndices.add(indexToSelect);
            }

            this.min = indexToSelect;
            this.max = indexToSelect;
        }

        public void toggleMultiSelection(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("Index must be non-negative");
            }

            if (!selectedIndices.contains(index)) {
                selectedIndices.add(index);
            } else {
                selectedIndices.remove(Integer.valueOf(index));
            }

            this.min = Collections.min(selectedIndices);
            this.max = Collections.max(selectedIndices);
        }

        public Range getSelectionBoundaries() {
            if (selectedIndices.isEmpty()) {
                // return empty
                return Range.between(0, 0);
            }
            return Range.between(min, max + 1);
        }
    }
}
