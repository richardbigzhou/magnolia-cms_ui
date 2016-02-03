/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.icon.connector;

import info.magnolia.ui.vaadin.gwt.client.icon.widget.IconWidget;
import info.magnolia.ui.vaadin.icon.CompositeIcon;

import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.shared.ui.AbstractLayoutState;
import com.vaadin.shared.ui.Connect;

/**
 * Client-side connector for {@link CompositeIcon} component.
 */
@Connect(CompositeIcon.class)
public class CompositeIconConnector extends AbstractLayoutConnector {


    @Override
    public IconWidget getWidget() {
        return (IconWidget) super.getWidget();
    }

    @Override
    protected IconWidget createWidget() {
        return new IconWidget();
    }

    @Override
    protected AbstractLayoutState createState() {
        return new AbstractLayoutState();
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {

        boolean processedRoot = false;
        for (final ComponentConnector cc : getChildComponents()) {
            if (cc instanceof IconConnector) {
                if (!processedRoot) {
                    processedRoot = true;
                } else {
                    ((IconConnector) cc).setInnerIcon(true);
                }
                ((IconConnector) cc).getWidget().updateInnerStyles();
                getWidget().getElement().appendChild(cc.getWidget().getElement());
            }
        }
    }

}
