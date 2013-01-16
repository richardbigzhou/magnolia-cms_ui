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
package info.magnolia.ui.vaadin.gwt.client.form.connector;

import info.magnolia.ui.vaadin.form.Form;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.ActionFiringServerRpc;
import info.magnolia.ui.vaadin.gwt.client.editorlike.connector.EditorLikeComponentConnector;
import info.magnolia.ui.vaadin.gwt.client.form.rpc.FormServerRpc;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormFieldWrapper;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormView;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormView.Presenter;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormViewImpl;

import com.google.gwt.dom.client.Style.Unit;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * FormConnector.
 */
@Connect(Form.class)
public class FormConnector extends EditorLikeComponentConnector<FormView.Presenter, FormView> {

    private final ActionFiringServerRpc actionRpc = RpcProxy.create(ActionFiringServerRpc.class, this);

    private final FormServerRpc focusRpc = RpcProxy.create(FormServerRpc.class, this);
    @Override
    protected void init() {
        super.init();
        getLayoutManager().addElementResizeListener(getView().getHeaderElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                getView().getContentElement().getStyle().setTop(e.getLayoutManager().getOuterHeight(e.getElement()), Unit.PX);
            }
        });
    }

    @Override
    protected FormView createView() {
        final FormView view = new FormViewImpl();
        return view;
    }

    @Override
    protected Presenter createPresenter() {
        return new Presenter() {

            @Override
            public void fireAction(String action) {
                actionRpc.fireAction(action);
            }

            @Override
            public void runLayout() {
                getLayoutManager().setNeedsMeasure(FormConnector.this);
            }

            @Override
            public void jumpToNextError(FormFieldWrapper fieldWrapper) {
                focusRpc.focusNextProblematicField(Util.findConnectorFor(fieldWrapper.getField()));
            }
        };
    }

    @Override
    protected void updateActionsFromState() {
        /**
         * Quite an ugly hack caused by rather complex (a bit crazy) mutual integration of
         * FormDialog and Form (action arrangement instructions tend to overlap and we end up with
         * no actions at all - so we suppress them for the form).
         */
        if (!getState().actionsSuppressed) {
            super.updateActionsFromState();
        }
    }

    @Override
    public FormState getState() {
        return (FormState) super.getState();
    }

    @Override
    protected FormState createState() {
        return new FormState();
    }
}
