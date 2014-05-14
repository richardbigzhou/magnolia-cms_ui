/**
 * This file Copyright (c) 2014 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.api.pulse.task;

/**
 * ItemPresenter.
 */
public interface ItemPresenter {

    void setListener(Listener listener);

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        void showList();

        void updateDetailView(String itemId);
    }
}
