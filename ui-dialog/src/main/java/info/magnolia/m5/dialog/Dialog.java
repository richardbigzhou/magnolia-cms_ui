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
package info.magnolia.m5.dialog;


import info.magnolia.m5.dialog.gwt.client.VDialog;

import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * Server side implementation of the MagnoliaShell container.
 *
 * @author apchelintcev
 */
@SuppressWarnings("serial")
@ClientWidget(value=VDialog.class, loadStyle = LoadStyle.EAGER)
public class Dialog extends AbstractComponent implements ServerSideHandler {


    protected ServerSideProxy proxy = new ServerSideProxy(this) {{
        register("save", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                // do something
            }
        });

        register("cancel", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                // do something
            }
        });
    }};

    public Dialog() {
        super();
        setImmediate(true);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        proxy.paintContent(target);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    @Override
    public void attach() {
        super.attach();
    }

    @Override
    public void detach() {
        super.detach();
    }

    /* (non-Javadoc)
     * @see org.vaadin.rpc.ServerSideHandler#initRequestFromClient()
     */
    @Override
    public Object[] initRequestFromClient() {
        return new Object[] {};
    }

    /* (non-Javadoc)
     * @see org.vaadin.rpc.ServerSideHandler#callFromClient(java.lang.String, java.lang.Object[])
     */
    @Override
    public void callFromClient(String method, Object[] params) {
        // TODO Auto-generated method stub

    }

    public void addTab(String tabName) {
        proxy.call("addTab", tabName);
    }

    public void addField(String tabName, String fieldLabel) {
        proxy.call("addField", tabName, fieldLabel);
    }


}
