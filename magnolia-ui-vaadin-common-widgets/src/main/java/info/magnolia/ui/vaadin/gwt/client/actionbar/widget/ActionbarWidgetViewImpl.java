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
package info.magnolia.ui.vaadin.gwt.client.actionbar.widget;

import info.magnolia.ui.vaadin.gwt.client.actionbar.event.ActionTriggerEvent;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarSection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;

/**
 * The Class VActionbarViewImpl, GWT implementation for the VActionbarView interface.
 */
public class ActionbarWidgetViewImpl extends ComplexPanel implements ActionbarWidgetView, ActionTriggerEvent.Handler {

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

    private final boolean isDeviceTablet = isDeviceTablet();;

    private boolean isToggledOpen = false;

    private boolean isFullScreen = false;

    private final TouchDelegate delegate = new TouchDelegate(toggleButton);

    private final Map<String, ActionbarSectionWidget> sections = new LinkedHashMap<String, ActionbarSectionWidget>();

    public ActionbarWidgetViewImpl(final EventBus eventBus, Presenter presenter) {
        setElement(root);
        addStyleName(CLASSNAME);

        this.presenter = presenter;
        this.eventBus = eventBus;
        this.eventBus.addHandler(ActionTriggerEvent.TYPE, this);

        createToggleControl();
        createFullScreenControl();

        isToggledOpen = !isDeviceTablet;
        actualizeToggleState(isToggledOpen);
    }

    /**
     * Determine if device is tablet. Allows option to add a querystring parameter of tablet=true
     * for testing.
     * TODO: Christopher Zimmermann - there should be only one instance of this code ithe project.
     * 
     * @return Whether device is tablet.
     */
    private boolean isDeviceTablet() {
        return !(MGWT.getOsDetection().isDesktop() || Window.Location.getQueryString().indexOf("tablet=true") >= 0);
    }

    private void createToggleControl() {

        toggleButton.addStyleName(CLASSNAME_TOGGLE);
        add(toggleButton, root);

        toggleButtonIcon.addClassName("v-actionbar-toggle-icon");
        toggleButton.getElement().appendChild(toggleButtonIcon);

        DOM.sinkEvents(toggleButton.getElement(), Event.TOUCHEVENTS);
        delegate.addTouchStartHandler(new TouchStartHandler() {
            @Override
            public void onTouchStart(TouchStartEvent event) {
                isToggledOpen = !isToggledOpen;
                presenter.setOpened(isToggledOpen);
            }
        });
    }

    /*
     * Actions positions in tablet mode are set via row_x and col_x classes.
     * These need to be updated anytime a section is hidden or shown.
     */
    @Override
    public void refreshActionsPositionsTablet() {

        if (!isDeviceTablet) {
            return;
        }

        tabletRow = -1; // Used to assign rows and columns to each action item
        tabletColumn = 0;

        for (final ActionbarSectionWidget section : sections.values()) {

            // if section is visible - then update rows & cols
            if (section.isVisible()) {

                for (final VActionbarGroup group : section.getGroups().values()) {

                    tabletColumn = 0;
                    tabletRow++;

                    for (ActionbarItemWidget action : group.getActions()) {
                        // Add a flyout indicator if this is the first action and there are other
                        // actions
                        if (group.getNumActions() > 1) {
                            if (tabletColumn == 0) {
                                action.addStyleName("flyout");
                            }
                        } else {
                            action.removeStyleName("flyout");
                        }
                        ((VActionbarItemTablet) action).setRow(tabletRow);
                        ((VActionbarItemTablet) action).setColumn(tabletColumn);
                        tabletColumn++;
                    }
                }
            }
        }
        setToggleAndFullScreenButtonHeights(tabletRow);
    }

    private void createFullScreenControl() {
        DOM.sinkEvents(fullScreenButton.getElement(), Event.TOUCHEVENTS);
        add(fullScreenButton, root);

        fullScreenButton.addStyleName(CLASSNAME_FULLSCREEN);
        fullScreenButtonIcon.addClassName("v-actionbar-fullscreen-icon");
        fullScreenButtonIcon.addClassName("icon-open-fullscreen");
        fullScreenButton.getElement().appendChild(fullScreenButtonIcon);
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
        if (isFullScreen) {
            fullScreenButtonIcon.addClassName("icon-close-fullscreen");
            fullScreenButtonIcon.removeClassName("icon-open-fullscreen");
        } else {
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
            toggleButtonIcon.addClassName("open");// NOTE:CLZ:With icon fonts this class name will change.
        } else {
            toggleButtonIcon.removeClassName("open");// NOTE:CLZ:With icon fonts this class name will change.
        }
        if (isDeviceTablet) {
            for (final ActionbarSectionWidget section : sections.values()) {
                for (final VActionbarGroup group : section.getGroups().values()) {
                    group.setOpenHorizontally(isOpen);
                }
            }
        }
    }

    @Override
    public Map<String, ActionbarSectionWidget> getSections() {
        return sections;
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    public void addSection(ActionbarSection sectionParams) {
        ActionbarSectionWidget section = new ActionbarSectionWidget(sectionParams);
        sections.put(sectionParams.getName(), section);
        add(section, root);
    }

    public void addAction(ActionbarItem actionParams, String sectionName) {
        ActionbarSectionWidget section = sections.get(sectionName);
        if (section != null) {
            VActionbarGroup group = section.getGroups().get(actionParams.getGroupName());
            if (group == null) {
                tabletColumn = 0;
                tabletRow++;
                group = new VActionbarGroup(actionParams.getGroupName());
                section.addGroup(group);
                setToggleAndFullScreenButtonHeights(tabletRow);
            }

            ActionbarItemWidget action;
            if (isDeviceTablet) {
                action = new VActionbarItemTablet(actionParams, group, eventBus);
                ((VActionbarItemTablet) action).setRow(tabletRow);
                ((VActionbarItemTablet) action).setColumn(tabletColumn);
                tabletColumn++;
            } else {
                action = new ActionbarItemWidget(actionParams, group, eventBus);
            }
            group.addAction(action);
        }
    }

    /**
     * For tablet mode, position these buttons at the bottom of the button stack.
     * 
     * @return
     */
    private void setToggleAndFullScreenButtonHeights(int tabletRow) {
        // Position toggleButton and fullScreenButton at bottom of stack.
        toggleButton.setStyleName(CLASSNAME_TOGGLE + " row-" + (tabletRow + 1));
        fullScreenButton.setStyleName(CLASSNAME_FULLSCREEN + " row-" + (tabletRow + 1));
    }

    @Override
    public void onActionTriggered(ActionTriggerEvent event) {
        ActionbarItemWidget action = event.getSource();
        ActionbarSectionWidget section = (ActionbarSectionWidget) action.getParent().getParent();
        presenter.triggerAction(section.getName() + ":" + action.getName());
    }

    @Override
    public void setSections(Collection<ActionbarSection> newSections) {
        for (final ActionbarSectionWidget section : this.sections.values()) {
            remove(section);
        }
        sections.clear();
        for (final ActionbarSection section : newSections) {
            addSection(section);
            for (final ActionbarItem action : section.getActions().values()) {
                if (action.getIconFontId() == null) {
                    action.setResourceUrl(presenter.getIconResourceURL(action.getName()));
                }
                addAction(action, section.getName());
            }
        }
        refreshActionsPositionsTablet();
    }

    @Override
    public void setVisibleSections(Collection<ActionbarSection> visibleSections) {
        for (final ActionbarSectionWidget section : sections.values()) {
            section.setVisible(visibleSections.contains(section.getData()));
        }
        refreshActionsPositionsTablet();
    }

    @Override
    public void setDisabledActions(Collection<ActionbarItem> disabledActions) {
        for (final ActionbarSectionWidget section : sections.values()) {
            for (final VActionbarGroup group : section.getGroups().values()) {
                for (final ActionbarItemWidget action : group.getActions()) {
                    action.setEnabled(!disabledActions.contains(action.getData()));
                }
            }
        }
    }

    @Override
    public boolean isOpen() {
        return isToggledOpen;
    }

    @Override
    public void setOpen(boolean isOpen) {
        actualizeToggleState(isToggledOpen);
        presenter.forceLayout();
    }

    @Override
    public void setSectionPreview(String sectionName, String previewUrl) {
        ActionbarSectionWidget sectionWidget = sections.get(sectionName);
        if (sectionWidget != null) {
            sectionWidget.setPreview(new Image(previewUrl));
        }
    }

}
