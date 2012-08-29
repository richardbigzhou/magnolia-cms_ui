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
package info.magnolia.ui.widget.actionbar.gwt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.ui.client.MGWT;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.Icon;


/**
 * Vaadin implementation of Action bar client side (Presenter).
 */
@SuppressWarnings("serial")
public class VActionbar extends Composite implements Paintable, Container, ClientSideHandler, VActionbarView.Presenter {

    protected String paintableId;

    protected ApplicationConnection client;

    private final VActionbarView view;

    private final EventBus eventBus;

    private final ClientSideProxy proxy = new ClientSideProxy(this) {

        {

            register("addSection", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    final VActionbarSectionJSO section = VActionbarSectionJSO.parse(String.valueOf(params[0]));
                    view.addSection(section);
                }
            });

            register("removeSection", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    String sectionName = String.valueOf(params[0]);
                    view.removeSection(sectionName);
                }
            });

            register("addAction", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    String jsonIcon = String.valueOf(params[0]);
                    final VActionbarItemJSO action = VActionbarItemJSO.parse(jsonIcon);
                    String groupName = String.valueOf(params[1]);
                    String sectionName = String.valueOf(params[2]);

                    if (action.getIcon().startsWith("icon-")) {
                        view.addAction(action, groupName, sectionName);
                    } else {
                        Icon icon = null;
                        if (action.getIcon() != null) {
                            icon = new Icon(client, action.getIcon());
                        }
                        view.addAction(action, icon, groupName, sectionName);
                    }
                }
            });

            // ENABLE / DISABLE

            register("setActionEnabled", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    boolean enabled = Boolean.parseBoolean(String.valueOf(params[0]));
                    String actionName = String.valueOf(params[1]);
                    String sectionName = null;
                    if (params.length > 2) {
                        sectionName = String.valueOf(params[2]);
                    }

                    for (VActionbarItem action : findActions(actionName, null, sectionName)) {
                        action.setEnabled(enabled);
                    }
                }

            });

            register("setGroupEnabled", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    boolean enabled = Boolean.parseBoolean(String.valueOf(params[0]));
                    String groupName = String.valueOf(params[1]);
                    String sectionName = null;
                    if (params.length > 2) {
                        sectionName = String.valueOf(params[2]);
                    }

                    for (VActionbarItem action : findActions(null, groupName, sectionName)) {
                        action.setEnabled(enabled);
                    }
                }
            });

            register("setSectionVisible", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    boolean visible = Boolean.parseBoolean(String.valueOf(params[0]));
                    String sectionName = String.valueOf(params[1]);

                    VActionbarSection section = findSection(sectionName);
                    section.setVisible(visible);

                    view.refreshActionsPositionsTablet();
                }
            });

        }
    };

    public VActionbar() {
        super();
        eventBus = new SimpleEventBus();
        view = new VActionbarViewImpl(eventBus);
        view.setPresenter(this);
        initWidget(view.asWidget());
    }



    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        proxy.update(this, uidl, client);

        Iterator<Object> childIterator = uidl.getChildIterator();
        while (childIterator.hasNext()) {
            Object next = childIterator.next();
            if (next instanceof UIDL) {
                UIDL childUIDL = (UIDL) next;
                String[] chunks = childUIDL.getTag().split(":");
                if (chunks.length == 2 && chunks[1].equals("preview")) {
                    UIDL previewUidl = childUIDL.getChildUIDL(0);
                    final Paintable previewWidget = client.getPaintable(previewUidl);
                    VActionbarSection section = ((VActionbarViewImpl) view).getSections().get(chunks[0]);
                    if (section != null && previewWidget instanceof Widget) {
                        section.setPreview((Widget) previewWidget);
                        previewWidget.updateFromUIDL(previewUidl, client);
                    }
                }
            }
        }

        // TODO: Christopher Zimmermann Check if this is inefficient as updateFromUIDL might be called a fair amount.
        // But we need some way to ensure this is called once the actionbar is built.
        view.refreshActionsPositionsTablet();
        //GWT.log("actionbar uidl");
        //VConsole.log("actionbar uidl (vconsole)");
    }

    @Override
    public boolean initWidget(Object[] params) {
        if (!initIsDeviceTablet()) {
            setOpened(true);
        }
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        GWT.log("Unknown method call from server: " + method);
    }

    @Override
    public void triggerAction(String actionToken) {
        proxy.call("actionTriggered", actionToken);
    }

    @Override
    public void changeFullScreen(boolean isFullScreen){
        proxy.call("changeFullScreen",isFullScreen);
    };


    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(child.getOffsetWidth(), getOffsetHeight());
        }
        return new RenderSpace(getOffsetWidth(), getOffsetHeight());
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return view.hasChildComponent(component);
    }

    private List<VActionbarItem> findActions(String actionName, String groupName, String sectionName) {
        List<VActionbarItem> actions = new ArrayList<VActionbarItem>();
        List<VActionbarGroup> groups = findGroups(groupName, sectionName);
        for (VActionbarGroup group : groups) {
            if (group != null) {

                List<VActionbarItem> groupActions = group.getActions();
                if (actionName != null) {
                    for (VActionbarItem action : groupActions) {
                        if (action.getName().equals(actionName)) {
                            actions.add(action);
                        }
                    }
                } else {
                    actions.addAll(groupActions);
                }
            }
        }
        return actions;
    }

    private List<VActionbarGroup> findGroups(String groupName, String sectionName) {
        List<VActionbarGroup> groups = new ArrayList<VActionbarGroup>();
        List<VActionbarSection> sections = findSections(sectionName);
        for (VActionbarSection section : sections) {
            if (groupName != null) {
                groups.add(section.getGroups().get(groupName));
            } else {
                groups.addAll(section.getGroups().values());
            }
        }
        return groups;
    }

    private List<VActionbarSection> findSections(String sectionName) {
        List<VActionbarSection> sections = new ArrayList<VActionbarSection>();
        if (sectionName != null) {
            sections.add(view.getSections().get(sectionName));
        } else {
            sections.addAll(view.getSections().values());
        }
        return sections;
    }

    private VActionbarSection findSection(String sectionName) {
        if (sectionName != null) {
            return view.getSections().get(sectionName);
        }
        return null;
    }

    @Override
    public void forceLayout() {
        client.forceLayout();
    }

    @Override
    public void setOpened(boolean opened) {
        setStyleDependentName("open", opened);
        if (paintableId != null && client != null) {
            client.updateVariable(paintableId, "opened", opened, false);
        }
    }

    /**
     * Determine if device is tablet. Allows option to add a querystring parameter of tablet=true
     * for testing. TODO: Christopher Zimmermann - there should be only one instance of this code in
     * the project.
     * @return Whether device is tablet.
     */
    private boolean initIsDeviceTablet() {

        boolean isDeviceTabletOverride = Window.Location.getQueryString().indexOf("tablet=true") >= 0;
        if (!MGWT.getOsDetection().isDesktop() || isDeviceTabletOverride) {
            return true;
        } else {
            return false;
        }
    }

}
