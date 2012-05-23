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
package info.magnolia.m5vaadin.shell;


import info.magnolia.m5vaadin.shell.gwt.client.VMagnoliaShell;
import info.magnolia.m5vaadin.shell.gwt.client.VShellMessage.MessageType;
import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * Server side implementation of the MagnoliaShell container.
 * 
 * @author apchelintcev
 */
@SuppressWarnings("serial")
@ClientWidget(value=VMagnoliaShell.class, loadStyle = LoadStyle.EAGER)
public abstract class BaseMagnoliaShell extends AbstractComponent implements ServerSideHandler {

    private List<FragmentChangedHandler> handlers = new LinkedList<FragmentChangedHandler>();
    
    private ShellViewport appViewport = new ShellViewport(this);
    
    private ShellViewport shellAppViewport = new ShellViewport(this);
    
    private ShellViewport dialogViewport = new ShellViewport(this);
    
    private ShellViewport activeViewport = null;
    
    protected ServerSideProxy proxy = new ServerSideProxy(this) {{
        register("activateShellApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                navigateToShellApp(String.valueOf(params[0]), String.valueOf(params[1]));
            }
        });
        
        register("activateApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                navigateToApp(String.valueOf(params[0]));
            }
        });
        
        
        register("closeCurrentApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                closeCurrentApp();
            }
        });
    }};
    
    public BaseMagnoliaShell() {
        super();
        System.out.println("SHELL CREATED!");
        setImmediate(true);
    }
    
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        
        target.startTag("shellAppViewport");
        shellAppViewport.paint(target);
        target.endTag("shellAppViewport");
        
        target.startTag("appViewport");
        appViewport.paint(target);
        target.endTag("appViewport");
        
        target.startTag("dialogViewport");
        dialogViewport.paint(target);
        target.endTag("dialogViewport");
        
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
        shellAppViewport.attach();
        dialogViewport.attach();
        appViewport.attach();
        shellAppViewport.setParent(this);
        dialogViewport.setParent(this);
        appViewport.setParent(this);
    }
    
    @Override
    public void detach() {
        super.detach();
        shellAppViewport.detach();
        dialogViewport.detach();
        appViewport.detach();
    }
    
    public ShellViewport getAppViewport() {
        return appViewport;
    }
    
    public ShellViewport getShellAppViewport() {
        return shellAppViewport;
    }
    
    public ShellViewport getActiveViewport() {
        return activeViewport;
    }
    
    @Override
    public Object[] initRequestFromClient() {
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        System.out.println("Client called " + method);
    }

    public void addFragmentChangedHanlder(final FragmentChangedHandler handler) {
        handlers.add(handler);
    }
    
    public void removeFragmentChangedHanlder(final FragmentChangedHandler handler) {
        handlers.remove(handler);
    }
    
    protected void setActiveViewport(ShellViewport activeViewport) {
        if (this.activeViewport != activeViewport) {
            this.activeViewport = activeViewport;  
            proxy.call("activeViewportChanged");
        }
    }
    
    protected void navigateToApp(String appFragment) {
        doNavigateWithinViewport(appViewport, appFragment);
    }
    
    protected void navigateToShellApp(final String fragment, String parameter) {
        doNavigateWithinViewport(shellAppViewport, fragment + ":" + parameter);
    }
    
    protected void doNavigateWithinViewport(final ShellViewport viewport, final String fragment) {
        viewport.setCurrentShellFragment(fragment);
        setActiveViewport(viewport);
        notifyOnFragmentChanged(fragment);
        requestRepaint();
    }
    
    private void notifyOnFragmentChanged(final String fragment) {
        final Iterator<FragmentChangedHandler> it = handlers.iterator();
        final FragmentChangedEvent event = new FragmentChangedEvent(fragment);
        while (it.hasNext()) {
            it.next().onFragmentChanged(event);
        }
    }
    
    public void showError(String message) {
        proxy.call("showMessage", MessageType.ERROR.name(), message);
    }
    
    public void showWarning(String message) {
        proxy.call("showMessage", MessageType.WARNING.name(), message);
    }

    protected void removeDialog(Component dialog) {
        dialogViewport.removeComponent(dialog);
        requestRepaint();
    }
    
    protected void addDialog(Component dialog) {
        dialogViewport.addComponent(dialog);
        requestRepaint();
    }
    
    protected void closeCurrentApp() {
        appViewport.pop();
        if (appViewport.isEmpty()) {
            setActiveViewport(shellAppViewport);
        }
    }
}
