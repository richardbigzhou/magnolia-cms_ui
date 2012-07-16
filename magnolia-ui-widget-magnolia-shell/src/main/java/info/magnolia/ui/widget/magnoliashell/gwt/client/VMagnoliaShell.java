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
package info.magnolia.ui.widget.magnoliashell.gwt.client;

import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.util.JSONUtil;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.VAppsViewport;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.VShellAppsViewport;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.VShellViewport;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.vaadin.artur.icepush.client.ui.VICEPush;
import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Vaadin implementation of MagnoliaShell client side.
 */
@SuppressWarnings("serial")
public class VMagnoliaShell extends Composite implements HasWidgets, Container, ClientSideHandler, VMagnoliaShellView.Presenter {

    /**
     * Enumeration of possible viewport types.
     */
    public enum ViewportType {
        SHELL_APP_VIEWPORT("shell:"), APP_VIEWPORT("app:"), DIALOG_VIEWPORT("");

        private String fragmentPrefix;

        private ViewportType(String fragmentPrefix) {
            this.fragmentPrefix = fragmentPrefix;
        }

        public String getFragmentPrefix() {
            return fragmentPrefix;
        }

    }

    protected String paintableId;

    protected ApplicationConnection client;

    private List<String> registeredAppNames = new LinkedList<String>();
    
    private List<String> runningAppNames = new LinkedList<String>();
    
    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {

            register("onAppStarted", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    runningAppNames.add(String.valueOf(params[0]));
                }
            });

            register("onAppStopped", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    runningAppNames.remove(String.valueOf(params[0]));
                }
            });

            register("registerApps", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    registerApps(JSONUtil.parseStringArray(String.valueOf(params[0])));
                }
            });

            register("navigate", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String prefix = String.valueOf(params[0]);
                    final String token = params.length > 1 ? String.valueOf(params[1]) : "";
                    view.navigate(prefix, token);
                }
            });

            register("activeViewportChanged", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    ViewportType type = params.length > 0 ? ViewportType.valueOf(String.valueOf(params[0]))
                            : ViewportType.SHELL_APP_VIEWPORT;
                    view.changeActiveViewport(type);
                }
            });

            register("showMessage", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final MessageType type = MessageType.valueOf(String.valueOf(params[0]));
                    final String topic = String.valueOf(params[1]);
                    final String message = String.valueOf(params[2]);
                    final String id = String.valueOf(params[3]);
                    view.showMessage(type, topic, message, id);
                }
            });

            register("updateIndication", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final ShellAppType type = ShellAppType.valueOf(String.valueOf(params[0]));
                    final int increment = (Integer) params[1];
                    view.updateShellAppIndication(type, increment);
                }
            });
        }
    };

    private final VMagnoliaShellView view;

    private final EventBus eventBus;

    public VMagnoliaShell() {
        super();
        eventBus = new SimpleEventBus();
        view = new VMagnoliaShellViewImpl(eventBus);
        view.setPresenter(this);
        initWidget(view.asWidget());
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        for (final ViewportType viewportType : ViewportType.values()) {
            final UIDL tagUidl = uidl.getChildByTagName(viewportType.name());
            if (tagUidl != null) {
                final UIDL viewportUidl = tagUidl.getChildUIDL(0);
                final Paintable p = client.getPaintable(viewportUidl);
                if (p instanceof VShellViewport) {
                    final VShellViewport viewport = (VShellViewport) p;
                    switch (viewportType) {
                    case APP_VIEWPORT:
                        view.updateAppViewport((VAppsViewport)viewport);
                        break;
                    case SHELL_APP_VIEWPORT:
                        view.updateShellAppViewport((VShellAppsViewport)viewport);
                        break;
                    case DIALOG_VIEWPORT:
                        view.updateDialogs(viewport);
                        break;
                    }
                    p.updateFromUIDL(viewportUidl, client);
                    if (ViewportType.DIALOG_VIEWPORT == viewportType && viewport.getWidgetCount() == 0) {
                        view.removeDialogViewport();
                    }
                }
            }
        }
        updatePusher(uidl);
        proxy.update(this, uidl, client);
    }

    private void updatePusher(final UIDL uidl) {
        final UIDL pusherUidl = uidl.getChildByTagName("pusher");
        if (pusherUidl != null) {
            final Paintable pusherPaintable = client.getPaintable(pusherUidl.getChildUIDL(0));
            if (pusherPaintable instanceof VICEPush) {
                if (!hasChildComponent((VICEPush) pusherPaintable)) {
                    view.setPusher((VICEPush) pusherPaintable);
                }
                pusherPaintable.updateFromUIDL(pusherUidl.getChildUIDL(0), client);
            }
        }
    }

    @Override
    public void loadShellApp(final ShellAppType type, final String token) {
        proxy.call("activateShellApp", type.name().toLowerCase(), token);
    }

    @Override
    public void loadApp(String prefix, String token) {
        proxy.call("activateApp", prefix, token);
    }

    @Override
    public void updateViewportLayout(VShellViewport viewport) {
        client.runDescendentsLayout(viewport);
    }

    @Override
    public boolean initWidget(Object[] params) {
        History.fireCurrentHistoryState();
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unhandled RPC call from server: " + method);
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        final Iterator<Widget> it = view.iterator();
        boolean result = false;
        while (it.hasNext() && !result) {
            result = component == it.next();
        }
        return result;
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(view.getViewportWidth(), view.getViewportHeight());
        }
        return new RenderSpace();
    }

    @Override
    public void destroyChild(final Widget child) {
        if (child instanceof Paintable) {
            client.unregisterPaintable((Paintable) child);
        }
    }

    @Override
    public void closeCurrentApp() {
        proxy.call("closeCurrentApp");
    }

    @Override
    public void closeCurrentShellApp() {
        proxy.call("closeCurrentShellApp");
    }

    @Override
    public void removeMessage(String id) {
        proxy.call("removeMessage", id);
    }

    @Override
    public void setWidth(String width) {
        view.asWidget().setWidth(width);
        super.setWidth(width);
    }

    @Override
    public void add(Widget w) {
        view.add(w);
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return view.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return view.remove(w);
    }

    @Override
    public boolean isAppRegistered(String appName) {
        return registeredAppNames.contains(appName);
    }

    private void registerApps(JsArrayString appNames) {
        registeredAppNames.clear();
        for (int i = 0; i < appNames.length(); ++i) {
            registeredAppNames.add(appNames.get(i));
        }
    }

    @Override
    public boolean isAppRunning(String appName) {
        return runningAppNames.contains(appName);
    }

}
