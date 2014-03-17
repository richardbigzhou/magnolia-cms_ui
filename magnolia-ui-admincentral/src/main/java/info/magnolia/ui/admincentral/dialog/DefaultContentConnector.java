/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.ui.vaadin.integration.NullItem;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;

/**
 * Stub implementation of {@link ContentConnector} interface. Does no conversion between item id and item.
 */
public class DefaultContentConnector implements ContentConnector {

    @Override
    public String getItemUrlFragment(Object itemId) {
        return StringUtils.EMPTY;
    }

    @Override
    public Object getItemIdByUrlFragment(String urlFragment) {
        return new NullItem();
    }

    @Override
    public Object getDefaultItemId() {
        return new NullItem();
    }

    @Override
    public Item getItem(Object itemId) {
        return itemId instanceof Item ? (Item)itemId : new NullItem();
    }

    @Override
    public boolean canHandleItem(Object itemId) {
        return itemId instanceof Item;
    }
}
