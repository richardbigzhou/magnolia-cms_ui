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
package info.magnolia.ui.widget.magnoliashell;


import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell.ViewportType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VShellMessage.MessageType;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;

/**
 * Server side implementation of the MagnoliaShell container.
 *
 * @author apchelintcev
 */
@SuppressWarnings("serial")
@ClientWidget(value=VMagnoliaShell.class, loadStyle = LoadStyle.EAGER)
public abstract class BaseMagnoliaShell extends AbstractComponent implements ServerSideHandler {

    private List<FragmentChangedHandler> handlers = new LinkedList<FragmentChangedHandler>();

    private Map<ViewportType, ShellViewport> viewports = new EnumMap<ViewportType, ShellViewport>(ViewportType.class);

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

        register("closeCurrentShellApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                closeCurrentShellApp();
            }
        });
    }};

    public BaseMagnoliaShell() {
        super();
        setImmediate(true);
        viewports.put(ViewportType.SHELL_APP_VIEWPORT, new ShellViewport(this));
        viewports.put(ViewportType.APP_VIEWPORT, new ShellViewport(this));
        viewports.put(ViewportType.DIALOG_VIEWPORT, new ShellViewport(this));
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        final Iterator<Entry<ViewportType, ShellViewport>> it = viewports.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ViewportType, ShellViewport> entry = it.next();
            final String tagName = entry.getKey().name();
            target.startTag(tagName);
            entry.getValue().paint(target);
            target.endTag(tagName);
        }

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
        for (final ShellViewport viewport : viewports.values()) {
            viewport.attach();
            viewport.setParent(this);
        }
    }

    @Override
    public void detach() {
        super.detach();
        for (final ShellViewport viewport : viewports.values()) {
            viewport.detach();
        }
    }

    public ShellViewport getAppViewport() {
        return viewports.get(ViewportType.APP_VIEWPORT);
    }

    public ShellViewport getShellAppViewport() {
        return viewports.get(ViewportType.SHELL_APP_VIEWPORT);
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
        doNavigateWithinViewport(getAppViewport(), appFragment);
    }

    protected void navigateToShellApp(final String fragment, String parameter) {
        doNavigateWithinViewport(getShellAppViewport(), fragment + ":" + parameter);
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

    public void removeDialog(Component dialog) {
        viewports.get(ViewportType.DIALOG_VIEWPORT).removeComponent(dialog);
        requestRepaint();
    }

    protected void addDialog(Component dialog) {
        viewports.get(ViewportType.DIALOG_VIEWPORT).addComponent(dialog);
        requestRepaint();
    }

    protected void closeCurrentShellApp() {
        if (!getAppViewport().isEmpty()) {
            setActiveViewport(getAppViewport());
        } else {
            navigateToShellApp(ShellAppType.APPLAUNCHER.name(), "");
        }
    }

    protected void closeCurrentApp() {
        getAppViewport().pop();
    }
}
