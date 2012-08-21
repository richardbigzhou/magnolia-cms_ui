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

import info.magnolia.ui.widget.actionbar.gwt.client.event.ActionTriggerEvent;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.terminal.gwt.client.ui.Icon;


/**
 * The Class VActionbarViewImpl, GWT implementation for the VActionbarView interface.
 */
public class VActionbarViewImpl extends ComplexPanel implements VActionbarView, ActionTriggerEvent.Handler {

    public static final String CLASSNAME = "v-actionbar";

    private final Element root = DOM.createElement("section");

    private final FlowPanel toggleButton = new FlowPanel(); // Must be a widget so that it can
                                                            // capture events.

    private final Element toggleButtonIcon = DOM.createElement("span");

    private final EventBus eventBus;

    private Presenter presenter;

    private int tabletRow = -1; // Used to assign rows and columns to each action item

    private int tabletColumn = 0;

    private final boolean isDeviceTablet;

    private boolean isToggledOpen = false;

    private TouchDelegate delegate = new TouchDelegate(toggleButton);

    private final Map<String, VActionbarSection> sections = new LinkedHashMap<String, VActionbarSection>();

    public VActionbarViewImpl(final EventBus eventBus) {
        setElement(root);
        addStyleName(CLASSNAME);

        this.eventBus = eventBus;
        this.eventBus.addHandler(ActionTriggerEvent.TYPE, this);

        isDeviceTablet = initIsDeviceTablet();

        prepareToggling();

        if (isDeviceTablet) {
            isToggledOpen = false;
        } else {
            isToggledOpen = true;
        }

        actualizeToggleState(isToggledOpen);
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

    private void prepareToggling() {

        toggleButton.addStyleName("v-actionbar-toggle");
        add(toggleButton, root);

        toggleButtonIcon.addClassName("v-actionbar-toggle-icon");
        toggleButton.getElement().appendChild(toggleButtonIcon);

        DOM.sinkEvents(toggleButton.getElement(), Event.TOUCHEVENTS);

        delegate.addTouchStartHandler(new TouchStartHandler() {

            @Override
            public void onTouchStart(com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent event) {
                GWT.log("Toggler TouchStart");
                isToggledOpen = !isToggledOpen;
                actualizeToggleState(isToggledOpen);
                presenter.forceLayout();
            }
        });

        /*
           DOM.sinkEvents(toggleButton.getElement(), Event.ONMOUSEDOWN);
        /*
           toggleButton.addDomHandler(new MouseDownHandler() {
               @Override
               public void onMouseDown(MouseDownEvent event) {
                   isToggledOpen = !isToggledOpen;

                   actualizeToggleState(isToggledOpen);

                   presenter.forceLayout();
               }

           }, MouseDownEvent.getType());
        }     */
    }

    /**
     * Actualize the state of the actionbar 'openness' by setting classes on html elements.
     */
    private void actualizeToggleState(boolean isOpen) {
        if (isOpen) {
            toggleButtonIcon.addClassName("open");// NOTE:CLZ:With icon fonts this class name will
                                                  // change.

            root.addClassName("v-actionbar-open");

            // For Tablet: Add "open" style from all actions
            if (isDeviceTablet) {
                for (final VActionbarSection section : sections.values()) {
                    for (final VActionbarGroup group : section.getGroups().values()) {
                        group.openHorizontal();
                    }
                }
            }

        } else {
            toggleButtonIcon.removeClassName("open");// NOTE:CLZ:With icon fonts this class name
                                                     // will change.

            root.removeClassName("v-actionbar-open");

            // For Tablet: Remove "open" style from all actions
            if (isDeviceTablet) {
                for (final VActionbarSection section : sections.values()) {
                    for (final VActionbarGroup group : section.getGroups().values()) {
                        group.removeStyleName("open");
                    }
                }
            }

        }
    }

    @Override
    public Map<String, VActionbarSection> getSections() {
        return sections;
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void addSection(VActionbarSectionJSO sectionParams) {
        VActionbarSection section = new VActionbarSection(sectionParams);
        sections.put(sectionParams.getName(), section);
        add(section, root);
    }

    @Override
    public void removeSection(String sectionName) {
        VActionbarSection section = sections.remove(sectionName);
        section.removeFromParent();
    }

    @Override
    public void addAction(VActionbarItemJSO actionParams, Icon icon, String groupName, String sectionName) {
        VActionbarSection section = sections.get(sectionName);
        if (section != null) {
            VActionbarGroup group = section.getGroups().get(groupName);
            if (group == null) {
                tabletColumn = 0;
                tabletRow++;
                group = new VActionbarGroup(groupName);
                section.addGroup(group);

                // Position toggleButton button at bottom of stack.
                toggleButton.removeStyleName("row-" + (tabletRow));
                toggleButton.addStyleName("row-" + (tabletRow + 1));
            }
            String cssClasses = "row-" + tabletRow + " col-" + tabletColumn + " open";

            VActionbarItem action;

            if (isDeviceTablet) {
                action = new VActionbarItemTablet(actionParams, group, eventBus, icon, cssClasses);
            } else {
                action = new VActionbarItem(actionParams, group, eventBus, icon, cssClasses);
            }

            group.addAction(action);
            tabletColumn++;
        }
    }

    @Override
    public void addAction(VActionbarItemJSO actionParams, String groupName, String sectionName) {
        VActionbarSection section = sections.get(sectionName);
        if (section != null) {
            VActionbarGroup group = section.getGroups().get(groupName);
            if (group == null) {
                tabletColumn = 0;
                tabletRow++;
                group = new VActionbarGroup(groupName);
                section.addGroup(group);

                // Position toggleButton button at bottom of stack.
                toggleButton.removeStyleName("row-" + (tabletRow));
                toggleButton.addStyleName("row-" + (tabletRow + 1));
            }
            String cssClasses = "row-" + tabletRow + " col-" + tabletColumn + " open";

            VActionbarItem action;

            if (isDeviceTablet) {
                action = new VActionbarItemTablet(actionParams, group, eventBus, cssClasses);
            } else {
                action = new VActionbarItem(actionParams, group, eventBus, cssClasses);
            }

            group.addAction(action);
            tabletColumn++;
        }
    }

    @Override
    public void onActionTriggered(ActionTriggerEvent event) {
        VActionbarItem action = event.getSource();
        VActionbarSection section = getParentSection(action);
        presenter.triggerAction(section.getName() + ":" + action.getName());
    }

    private VActionbarSection getParentSection(VActionbarItem item) {
        // parent is group, grandparent is section
        return (VActionbarSection) item.getParent().getParent();
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        if (sections.containsValue(component)) {
            return true;
        } else {
            for (VActionbarSection section : sections.values()) {
                if (section.getWidgetIndex(component) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

}
