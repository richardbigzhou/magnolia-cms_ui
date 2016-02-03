/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.form.field.component;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;

/**
 * Base implementation of {@link ContentPreviewComponent}.<br>
 *
 * @param <T>.
 */
public abstract class AbstractContentPreviewComponent<T> extends CustomComponent implements ContentPreviewComponent<T> {

    protected String workspace;
    protected Component contentDetail;
    protected Component contentPreview;
    protected Layout rootLayout;

    public AbstractContentPreviewComponent(String workspace) {
        this.workspace = workspace;
    }

    /**
     * On value change: <br>
     * - Clear the Root component <br>
     * - Retrieve/Refresh the related item (based on the itemReference)<br>
     * - Refresh the Content Detail and Preview component.
     */
    @Override
    public void onValueChange(String itemReference) {
        T item = null;
        clearRootLayout();
        if (StringUtils.isNotBlank(itemReference)) {
            item = refreshItem(itemReference);
        }
        if (item != null) {
            contentDetail = refreshContentDetail(item);
            contentPreview = refreshContentPreview(item);
            refreshRootLayout();
        }
    }

    /**
     * Refresh the Content detail. <br>
     * This method should be triggered by {@link ContentPreviewComponent#onValueChange(String)} in case of value changes.
     */
    abstract protected Component refreshContentDetail(T item);

    /**
     * Refresh the Content Preview.
     * This method should be triggered by {@link ContentPreviewComponent#onValueChange(String)} in case of value changes.
     */
    abstract protected Component refreshContentPreview(T item);

    /**
     * Based on the item path, retrieve the corresponding item. This Item is the used to refresh the content component's.
     */
    abstract protected T refreshItem(String itemPath);

    /**
     * Clear the root layout.
     */
    protected void clearRootLayout() {
        this.rootLayout.setVisible(false);
        this.rootLayout.removeAllComponents();
        removeStyleName("done");
    }

    /**
     * Refresh the root layout.
     */
    protected void refreshRootLayout() {
        this.rootLayout.setVisible(true);
        addStyleName("done");
        rootLayout.addComponent(contentPreview);
        rootLayout.addComponent(contentDetail);
    }

}
