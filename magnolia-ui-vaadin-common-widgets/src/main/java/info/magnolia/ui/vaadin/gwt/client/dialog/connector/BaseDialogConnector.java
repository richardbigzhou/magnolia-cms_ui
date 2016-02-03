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
package info.magnolia.ui.vaadin.gwt.client.dialog.connector;

import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.DialogServerRpc;
import info.magnolia.ui.vaadin.gwt.client.dialog.widget.BaseDialogView;
import info.magnolia.ui.vaadin.gwt.client.dialog.widget.BaseDialogViewImpl;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.shared.ui.Connect;

/**
 * BaseDialogConnector.
 */
@Connect(BaseDialog.class)
public class BaseDialogConnector extends AbstractLayoutConnector implements BaseDialogView.Presenter {

    private final DialogServerRpc rpc = RpcProxy.create(DialogServerRpc.class, this);

    protected BaseDialogView createView() {
        return new BaseDialogViewImpl();
    }

    private BaseDialogView view;

    @Override
    protected void init() {
        super.init();
        addStateChangeHandler("caption", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                view.setCaption(getState().caption);
            }
        });

        addStateChangeHandler("hasCloseButton", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                if (getState().hasCloseButton) {
                    view.showCloseButton();
                }
            }
        });

        addStateChangeHandler("componentDescription", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                view.setDescription(getState().componentDescription);
            }
        });
    }

    @Override
    protected BaseDialogState createState() {
        return new BaseDialogState();
    }

    @Override
    public BaseDialogState getState() {
        return (BaseDialogState) super.getState();
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        if (this == connector) {
            view.setCaption(connector.getState().caption);
        }
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        updateContent();
        updateHeaderToolbar();
        updateFooterToolbar();
    }

    @Override
    public boolean delegateCaptionHandling() {
        return false;
    }

    protected ComponentConnector getContent() {
        return (ComponentConnector) getState().content;
    }

    protected ComponentConnector getHeader() {
        return (ComponentConnector) getState().headerToolbar;
    }

    protected ComponentConnector getFooter() {
        return (ComponentConnector) getState().footerToolbar;
    }

    protected void updateContent() {
        final ComponentConnector content = getContent();
        if (content != null) {
            this.view.setContent(content.getWidget());
        }
    }

    protected void updateHeaderToolbar() {
        final ComponentConnector header = getHeader();
        if (header != null) {
            this.view.setHeaderToolbar(header.getWidget());
        }
    }

    protected void updateFooterToolbar() {
        final ComponentConnector footer = getFooter();
        if (footer != null) {
            this.view.setFooterToolbar(footer.getWidget());
        }
    }

    @Override
    protected Widget createWidget() {
        this.view = createView();
        this.view.setPresenter(this);
        return view.asWidget();
    }

    @Override
    public void closeDialog() {
        rpc.closeSelf();
    }

    @Override
    public void setDescriptionVisibility(boolean isVisible) {
        rpc.setDescriptionVisibility(isVisible);
    }
}
