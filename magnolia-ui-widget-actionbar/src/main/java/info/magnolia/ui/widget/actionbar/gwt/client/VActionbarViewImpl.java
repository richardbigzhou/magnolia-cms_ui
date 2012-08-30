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

import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;
import info.magnolia.ui.widget.actionbar.gwt.client.event.ActionTriggerEvent;

//import java.awt.event.WindowAdapter;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public static final String CLASSNAME_TOGGLE = "v-actionbar-toggle";
    public static final String CLASSNAME_FULLSCREEN = "v-actionbar-fullscreen";

    private final Element root = DOM.createElement("section");

    private final FlowPanel toggleButton = new FlowPanel(); // Must be a widget so that it can capture events
    private final Element toggleButtonIcon = DOM.createElement("span");

    private final TouchPanel fullScreenButton = new TouchPanel(); // Must be a widget so that it can capture events
    private final Element fullScreenButtonIcon = DOM.createElement("span");

    private final EventBus eventBus;

    private Presenter presenter;

    private int tabletRow = -1; // Used to assign rows and columns to each action item

    private int tabletColumn = 0;

    private final boolean isDeviceTablet;

    private boolean isToggledOpen = false;

    private boolean isFullScreen = false;

    private final TouchDelegate delegate = new TouchDelegate(toggleButton);

    private final Map<String, VActionbarSection> sections = new LinkedHashMap<String, VActionbarSection>();

    public VActionbarViewImpl(final EventBus eventBus) {
        setElement(root);
        addStyleName(CLASSNAME);


        this.eventBus = eventBus;
        this.eventBus.addHandler(ActionTriggerEvent.TYPE, this);

        isDeviceTablet = initIsDeviceTablet();
        tabletRow = -1;

        prepareToggling();
        prepareFullScreenButton();

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

        toggleButton.addStyleName(CLASSNAME_TOGGLE);
        add(toggleButton, root);

        toggleButtonIcon.addClassName("v-actionbar-toggle-icon");
        toggleButton.getElement().appendChild(toggleButtonIcon);

        DOM.sinkEvents(toggleButton.getElement(), Event.TOUCHEVENTS);

        delegate.addTouchStartHandler(new TouchStartHandler() {

            @Override
            public void onTouchStart(com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent event) {
                isToggledOpen = !isToggledOpen;
                actualizeToggleState(isToggledOpen);
                presenter.forceLayout();
            }
        });
    }

    /*
     * Actions positions in tablet mode are set via row_x and col_x classes.
     * These need to be updated anytime a section is hidden or shown.
     */
    public void refreshActionsPositionsTablet(){

        if (!isDeviceTablet){
            return;
        }

        tabletRow = -1; // Used to assign rows and columns to each action item
        tabletColumn = 0;

         for (final VActionbarSection section : sections.values()) {

            //if section is visible - then update rows & cols
            if (section.isVisible()){

            for (final VActionbarGroup group : section.getGroups().values()) {

                    tabletColumn = 0;
                    tabletRow++;

                    for (VActionbarItem action : group.getActions()) {
                        String cssClasses = "row-" + tabletRow + " col-" + tabletColumn + " open";
                        action.resetStyleNames(cssClasses);
                        tabletColumn++;
                    }
                }
            }
        }
        setToggleAndFullScreenButtonHeights(tabletRow);
    }

    private void prepareFullScreenButton() {

        fullScreenButton.addStyleName(CLASSNAME_FULLSCREEN);

        add(fullScreenButton, root);

        fullScreenButtonIcon.addClassName("v-actionbar-fullscreen-icon");
        fullScreenButtonIcon.addClassName("icon-open-fullscreen");

        fullScreenButton.getElement().appendChild(fullScreenButtonIcon);

        DOM.sinkEvents(fullScreenButton.getElement(), Event.TOUCHEVENTS);

        fullScreenButton.addTouchStartHandler(new TouchStartHandler() {

            @Override
            public void onTouchStart(TouchStartEvent event) {
                isFullScreen = !isFullScreen;
                actualizeFullScreenState(isFullScreen);
            }
        });
    }

    /**
     * Actualize the state of the actionbar fullscreen button.
     */
    private void actualizeFullScreenState(boolean isFullScreen) {
         if (isFullScreen){
             fullScreenButtonIcon.addClassName("icon-close-fullscreen");
             fullScreenButtonIcon.removeClassName("icon-open-fullscreen");
         }   else{
             fullScreenButtonIcon.addClassName("icon-open-fullscreen");
             fullScreenButtonIcon.removeClassName("icon-close-fullscreen");
         }

        presenter.changeFullScreen(isFullScreen);
    }


    /**
     * Actualize the state of the actionbar 'openness' by setting classes on html elements.
     */
    private void actualizeToggleState(boolean isOpen) {
        if (isOpen) {
            toggleButtonIcon.addClassName("open");// NOTE:CLZ:With icon fonts this class name will
                                                  // change.

            if (presenter != null) {
                presenter.setOpened(true);
            }

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

            if (presenter != null) {
                presenter.setOpened(false);
            }

            // For Tablet: Remove "open" style from all actions
            if (isDeviceTablet) {
                for (final VActionbarSection section : sections.values()) {
                    for (final VActionbarGroup group : section.getGroups().values()) {
                        group.closeHorizontal();
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
                group = new VActionbarGroup(groupName);
                section.addGroup(group);
            }
            String cssClasses = "open";

            VActionbarItem action;

            if (isDeviceTablet) {
                action = new VActionbarItemTablet(actionParams, group, eventBus, icon, cssClasses);
            } else {
                action = new VActionbarItem(actionParams, group, eventBus, icon, cssClasses);
            }

            group.addAction(action);
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

                setToggleAndFullScreenButtonHeights(tabletRow);
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

    /**
     * For tablet mode, position these buttons at the bottom of the button stack.
     * @return
     */
    private void setToggleAndFullScreenButtonHeights(int tabletRow){
        // Position toggleButton and fullScreenButton at bottom of stack.

        toggleButton.setStyleName(CLASSNAME_TOGGLE + " row-" + (tabletRow + 1));

        fullScreenButton.setStyleName(CLASSNAME_FULLSCREEN + " row-" + (tabletRow + 1));
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
