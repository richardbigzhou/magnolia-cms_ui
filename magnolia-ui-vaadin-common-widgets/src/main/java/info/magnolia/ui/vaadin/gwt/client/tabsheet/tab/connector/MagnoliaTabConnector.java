/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.connector;

import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTab;

import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

/**
 * MagnoliaTabConnector.
 */
@Connect(MagnoliaTab.class)
public class MagnoliaTabConnector extends AbstractSingleComponentContainerConnector {

    @Override
    protected void init() {
        super.init();
        addStateChangeHandler("isClosable", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getWidget().getLabel().setClosable(getState().isClosable);
            }
        });

        addStateChangeHandler("hasError", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getWidget().getLabel().setHasError(getState().hasError);
            }
        });

        addStateChangeHandler("isNotificationHidden", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getWidget().getLabel().hideNotification();
            }
        });

        addStateChangeHandler("notification", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getWidget().getLabel().updateNotification(getState().notification);
            }
        });

        addStateChangeHandler("isActive", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getWidget().getLabel().updateNotification(getState().notification);
                if (getState().isActive) {
                    getWidget().removeStyleName("inactive");
                } else {
                    getWidget().addStyleName("inactive");
                }

            }
        });
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
    }

    @Override
    public MagnoliaTabWidget getWidget() {
        return (MagnoliaTabWidget) super.getWidget();
    }

    @Override
    protected MagnoliaTabWidget createWidget() {
        return new MagnoliaTabWidget(this);
    }

    @Override
    public MagnoliaTabState getState() {
        return (MagnoliaTabState) super.getState();
    }

    @Override
    protected MagnoliaTabState createState() {
        return new MagnoliaTabState();
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent e) {
        if (!e.getOldChildren().isEmpty()) {
            final ComponentConnector oldContent = e.getOldChildren().get(0);
            getWidget().remove(oldContent.getWidget());
        }

        if (getContent() != null) {
            getWidget().setWidget(getContent().getWidget());
        }
    }
}
