/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.actionbar.connector;

import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.gwt.client.actionbar.rpc.ActionbarServerRpc;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarSection;
import info.magnolia.ui.vaadin.gwt.client.actionbar.widget.ActionbarWidgetView;
import info.magnolia.ui.vaadin.gwt.client.actionbar.widget.ActionbarWidgetViewImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.ui.client.MGWT;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * {@link ActionbarConnector}.
 */
@Connect(Actionbar.class)
public class ActionbarConnector extends AbstractComponentConnector implements ActionbarWidgetView.Presenter {

    private ActionbarWidgetView view;

    private final EventBus eventBus = new SimpleEventBus();

    private final ActionbarServerRpc rpc = RpcProxy.create(ActionbarServerRpc.class, this);

    private final boolean isTablet = !(MGWT.getOsDetection().isDesktop() || Window.Location.getQueryString().indexOf("tablet=true") >= 0);

    private final StateChangeHandler sectionRearrangementHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            List<ActionbarSection> sections = new ArrayList<ActionbarSection>(getState().sections.values());
            Collections.sort(sections, new Comparator<ActionbarSection>() {
                @Override
                public int compare(ActionbarSection o1, ActionbarSection o2) {
                    Integer idx1 = getState().sectionOrder.indexOf(o1.getName());
                    Integer idx2 = getState().sectionOrder.indexOf(o2.getName());
                    return idx1.compareTo(idx2);
                }
            });
            view.setSections(sections);
            view.setDisabledActions(getState().disabledActions);
            view.setVisibleSections(getState().visibleSections);
        }
    };

    private final StateChangeHandler previewChangeHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    for (String sectionName : getState().sections.keySet()) {
                        String previewUrl = getResourceUrl(sectionName);
                        if (previewUrl != null) {
                            view.setSectionPreview(sectionName, previewUrl);
                        }
                    }
                }
            });
        }
    };

    private final StateChangeHandler visibleSectionSetChangeHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    view.setVisibleSections(getState().visibleSections);
                }
            });
        }
    };

    private final StateChangeHandler enabledActionSetChangeHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    view.setDisabledActions(getState().disabledActions);
                }
            });
        }
    };

    private final StateChangeHandler collapseChangeHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            view.setOpen(getState().isOpen);
        }
    };

    @Override
    protected void init() {
        super.init();
        addStateChangeHandler(previewChangeHandler);
        addStateChangeHandler("sections", sectionRearrangementHandler);
        addStateChangeHandler("visibleSections", visibleSectionSetChangeHandler);
        addStateChangeHandler("disabledActions", enabledActionSetChangeHandler);
        addStateChangeHandler("isOpen", collapseChangeHandler);

        if (isDeviceTablet()) {
            setOpened(true);
        }

        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() {

            @Override
            public void onElementResize(ElementResizeEvent e) {
                getWidget().updateLayout();
            }
        });
    }

    @Override
    public ActionbarWidgetViewImpl getWidget() {
        return (ActionbarWidgetViewImpl) super.getWidget();
    }

    @Override
    protected Widget createWidget() {
        this.view = new ActionbarWidgetViewImpl(eventBus, this);
        return this.view.asWidget();
    }

    @Override
    public ActionbarState getState() {
        return (ActionbarState) super.getState();
    }

    @Override
    public void triggerAction(String actionToken) {
        rpc.onActionTriggered(actionToken);
    }

    @Override
    public void setOpened(boolean isOpen) {
        rpc.setOpen(isOpen);
    }

    @Override
    public void forceLayout() {
        getLayoutManager().setNeedsMeasure(this);
    }

    @Override
    public String getIconResourceURL(String actionName) {
        return getResourceUrl(actionName);
    }


    /**
     * Determine if device is tablet. Allows option to add a querystring parameter of tablet=true
     * for testing.
     *
     * @return Whether device is tablet.
     */
    @Override
    public boolean isDeviceTablet() {
        return isTablet;
    }
}
