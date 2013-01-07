/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.form.tab.connector;

import info.magnolia.ui.vaadin.form.tab.MagnoliaFormTab;
import info.magnolia.ui.vaadin.gwt.client.form.formsection.event.ValidationChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.form.tab.rpc.FormTabServerRpc;
import info.magnolia.ui.vaadin.gwt.client.form.tab.widget.FormTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.connector.MagnoliaTabConnector;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.shared.ui.Connect;

/**
 * FormTabConnector.
 */
@Connect(MagnoliaFormTab.class)
public class FormTabConnector extends MagnoliaTabConnector {

    private final FormTabServerRpc rpc = RpcProxy.create(FormTabServerRpc.class, this);

    @Override
    protected void init() {
        super.init();
        addStateChangeHandler("hasError", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getWidget().fireEvent(new ValidationChangedEvent());
            }
        });
    }

    public void setHasErrors(boolean hasErrors) {
        rpc.setHasErrors(hasErrors);
    }

    @Override
    protected MagnoliaTabWidget createWidget() {
        return new FormTabWidget(this);
    }
}
