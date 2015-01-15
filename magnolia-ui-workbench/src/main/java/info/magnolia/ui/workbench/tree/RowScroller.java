/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.workbench.tree;

import com.vaadin.annotations.JavaScript;
import com.vaadin.data.Container;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.Table;

/**
 * Brings a table row into view.
 */
@JavaScript("rowscroller_connector.js")
public class RowScroller extends AbstractJavaScriptExtension {

    public static final String MGNL_TABLE = "mgnlTable";

    public RowScroller(Table table) {
        extend(table);
    }

    public void bringRowIntoView(Object rowId) {
        scrollRowIntoView(rowId);
    }

    @Override
    protected Class<? extends ClientConnector> getSupportedParentType() {
        return Table.class;
    }

    private void scrollRowIntoView(Object itemId) {
        int itemIndex = getItemIndex(itemId);
        if (itemIndex >= 0) {
            callFunction("scrollRowIntoView", itemIndex);
        }
    }

    @Override
    public Table getParent() {
        return (Table) super.getParent();
    }

    protected int getItemIndex(Object itemId) {
        Container container = getParent().getContainerDataSource();
        return ((Container.Indexed)container).indexOfId(itemId);
    }

    @Override
    public void attach() {
        super.attach();
        getParent().addStyleName(MGNL_TABLE + getParent().getConnectorId());
    }
}
