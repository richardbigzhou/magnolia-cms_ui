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
package info.magnolia.ui.vaadin.gwt.client.form.connector;

import info.magnolia.ui.vaadin.form.Form;
import info.magnolia.ui.vaadin.gwt.client.form.rpc.FormServerRpc;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormFieldWrapper;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormView;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormViewImpl;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client-side connector, a counter-part for {@link Form}.
 */
@Connect(Form.class)
public class FormConnector extends AbstractSingleComponentContainerConnector implements FormView.Presenter {

    private final FormServerRpc focusRpc = RpcProxy.create(FormServerRpc.class, this);

    private FormView view;

    @Override
    protected void init() {
        super.init();
        view.setPresenter(this);
        addStateChangeHandler("descriptionsVisible", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getView().setDescriptionVisible(getState().descriptionsVisible);
            }
        });

        addStateChangeHandler("errorAmount", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getView().setErrorAmount(getState().errorAmount);
            }
        });

        addStateChangeHandler("errorsLabel", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getView().setErrorsLabel(getState().errorsLabel);
            }
        });

        addStateChangeHandler("nextErrorLabel", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                getView().setNextErrorLabel(getState().nextErrorLabel);
            }
        });
    }

    @Override
    protected Widget createWidget() {
        view = new FormViewImpl();
        return view.asWidget();
    }

    protected FormView getView() {
        if (view == null) {
            createWidget();
        }
        return view;
    }

    @Override
    public FormState getState() {
        return (FormState) super.getState();
    }

    @Override
    protected FormState createState() {
        return new FormState();
    }

    @Override
    public void jumpToNextError(FormFieldWrapper fieldWrapper) {
        ComponentConnector cc = fieldWrapper == null ? null : Util.findConnectorFor(fieldWrapper.getField());
        focusRpc.focusNextProblematicField(cc);
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        //NOP
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        updateContent();
    }

    protected void updateContent() {
        final ComponentConnector content = getContent();
        if (content != null) {
            this.view.setContent(content.getWidget());
        }
    }

}
