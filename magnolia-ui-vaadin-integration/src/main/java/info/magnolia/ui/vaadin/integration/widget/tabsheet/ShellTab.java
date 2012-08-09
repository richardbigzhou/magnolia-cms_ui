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
package info.magnolia.ui.vaadin.integration.widget.tabsheet;

import info.magnolia.ui.vaadin.integration.widget.client.tabsheet.VShellTab;

import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.ComponentContainer;

/**
 * A tab in the shell tabsheet.
 */
@SuppressWarnings("serial")
@ClientWidget(value=VShellTab.class, loadStyle = LoadStyle.EAGER)
public class ShellTab extends SimplePanel implements ServerSideHandler {

    private String tabId = null;
    
    private boolean isClosable = false;
    
    private boolean hasError = false;
    
    private String notification = null;

    private ServerSideProxy proxy = new ServerSideProxy(this);
    
    public ShellTab(final String caption, final ComponentContainer c) {
        super(c);
        setSizeFull();
        setImmediate(true);
        setCaption(caption);
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
    
    public void setTabId(String tabId) {
        this.tabId = tabId;
        proxy.callOnce("setTabId", tabId);
    }
    
    public String getTabId() {
        return tabId;
    }

    public boolean isClosable() {
        return isClosable;
    }
    
    public void setClosable(boolean isClosable) {
        this.isClosable = isClosable;
        proxy.callOnce("setClosable", isClosable);
    }

    public void setNotification(String text) {
        this.notification = text;
        proxy.callOnce("updateNotification", text);
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        proxy.callOnce("setHasError", hasError);
    }
    
    public void hideNotification() {
        proxy.callOnce("hideNotification");
        this.notification = null;
    }
    
    public String getNotification() {
        return notification;
    }
    
    public boolean hasNotification() {
        return this.notification != null;
    }

    public boolean hasError() {
        return hasError;
    }
    
    @Override
    public Object[] initRequestFromClient() {
        if (tabId != null) {
            proxy.callOnce("setTabId", tabId);
            proxy.callOnce("setClosable", isClosable);
            proxy.callOnce("setError", hasError);
            if (notification != null) {
                proxy.callOnce("updateNotification", notification);   
            } else {
                proxy.callOnce("hideNotification");
            }
        }
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unhandled method call from client: " + method);
    }

}
