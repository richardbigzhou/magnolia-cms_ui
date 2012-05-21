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
package info.magnolia.m5.dialog.gwt.client;

import java.util.Set;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;


/**
 * Vaadin implementation of MagnoliaShell client side.
 */
@SuppressWarnings("serial")
public class VDialog extends VDialogGWT implements Container, ClientSideHandler {

    protected String paintableId;

    protected ApplicationConnection client;

    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("addTab", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    addTab((String)params[0]);
                }
            });
            register("addField", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    addField((String)params[0], (String)params[1]);
                }
            });
        }
    };

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();

        proxy.update(this, uidl, client);
    }


    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#hasChildComponent(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean hasChildComponent(Widget component) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#updateCaption(com.vaadin.terminal.gwt.client.Paintable, com.vaadin.terminal.gwt.client.UIDL)
     */
    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#requestLayout(java.util.Set)
     */
    @Override
    public boolean requestLayout(Set<Paintable> children) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#getAllocatedSpace(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.vaadin.rpc.client.ClientSideHandler#initWidget(java.lang.Object[])
     */
    @Override
    public boolean initWidget(Object[] params) {
        // TODO Auto-generated method stub
        return false;
    }


    /* (non-Javadoc)
     * @see org.vaadin.rpc.client.ClientSideHandler#handleCallFromServer(java.lang.String, java.lang.Object[])
     */
    @Override
    public void handleCallFromServer(String method, Object[] params) {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#replaceChildComponent(com.google.gwt.user.client.ui.Widget, com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO Auto-generated method stub

    }



}
