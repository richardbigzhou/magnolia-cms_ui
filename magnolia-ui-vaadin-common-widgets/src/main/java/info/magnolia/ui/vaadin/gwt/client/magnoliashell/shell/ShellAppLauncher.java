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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.AppActivatedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppNavigationEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ViewportCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.handler.ShellNavigationAdapter;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.handler.ShellNavigationHandler;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;


/**
 * Navigation bar.
 */
public class ShellAppLauncher extends FlowPanel {

    private final static int DIVET_ANIMATION_SPEED = 200;

    private final static String ID = "main-launcher";

    private final ShellNavigationHandler navigationHandler = new ShellNavigationAdapter() {

        @Override
        public void onAppActivated(AppActivatedEvent event) {
            if (event.isShellApp()) {
                activateControl(ShellAppType.valueOf(event.getAppId().toUpperCase()));
            }
        }
    };

    private class NavigatorButton extends FlowPanel {

        /**
         * TODO - HANDLE ICONS.
         */
        //private final GwtBadgeIcon indicator = new GwtBadgeIcon();

        private final TouchDelegate delegate = new TouchDelegate(this);

        public NavigatorButton(final ShellAppType type) {
            super();
            addStyleName("btn-shell");
            Element root = getElement();
            root.setId("btn-" + type.getCssClass());
            root.addClassName("icon-" + type.getCssClass());

            /**
             * TODO:
             * indicator.updateFillColor("#fff");
             * indicator.updateStrokeColor("#689600");
             * indicator.updateOutline(true);
             * root.appendChild(indicator.getElement()); 
             */
            
            

            DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);
            delegate.addTouchEndHandler(new com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler() {
                @Override
                public void onTouchEnd(com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent event) {
                    // Has user clicked on the active shell app?
                    if (type == getActiveShellType()) {
                        // if open then close it.
                        eventBus.fireEvent(new ViewportCloseEvent(ViewportType.SHELL_APP));
                    } else {
                        log("Going to " + type);
                        // If closed, then open it.
                        navigateToShellApp(type);
                    }
                }
            });
        }

        public void setIndication(int indication) {
            //indicator.updateValue(indication);
        }
    };

    private final native void log(String msg) /*-{
        $wnd.console.log(msg);
    }-*/;

    private int expandedHeight = 0;

    private final Element divetWrapper = DOM.createDiv();

    private final TouchPanel logo = new TouchPanel();

    private final Element logoImg = DOM.createImg();

    private final Image divet = new Image(VShellImageBundle.BUNDLE.getDivetGreen());

    private final Map<ShellAppType, NavigatorButton> controlsMap = new EnumMap<ShellAppType, NavigatorButton>(ShellAppType.class);

    private final EventBus eventBus;

    public ShellAppLauncher(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        getElement().setId(ID);
        construct();
        bindHandlers();

    }

    private void navigateToShellApp(final ShellAppType type) {
        eventBus.fireEvent(new ShellAppNavigationEvent(type, ""));
    }

    private void construct() {
        divetWrapper.setId("divet");
        logoImg.setId("logo");
        String baseUrl = GWT.getModuleBaseURL().replace("widgetsets/" + GWT.getModuleName() + "/", "");
        logoImg.setAttribute("src", baseUrl + "themes/admincentraltheme/img/logo-magnolia.svg");

        logo.getElement().appendChild(logoImg);
        add(logo);

        getElement().appendChild(divetWrapper);
        add(divet, divetWrapper);
        for (final ShellAppType appType : ShellAppType.values()) {
            final NavigatorButton w = new NavigatorButton(appType);
            controlsMap.put(appType, w);
            add(w);
        }
        divet.setVisible(false);
    }

    private void bindHandlers() {
        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);
        logo.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                emergencyRestartApplication();
            }
        });

    }

    /**
     * Restart the application by appending the &restartApplication querystring to the URL. This is
     * handy as the application is not totally stable yet. TODO: Christopher Zimmermann CLZ
     * Developer Preview feature.
     */
    private void emergencyRestartApplication() {
        String newHref = Window.Location.getPath() + "?restartApplication";
        Window.Location.assign(newHref);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        expandedHeight = getOffsetHeight();
        getElement().getStyle().setTop(-60, Unit.PX);
        JQueryWrapper.select(getElement()).animate(250, new AnimationSettings() {{
                setProperty("top", 0);
        }});
        eventBus.addHandler(AppActivatedEvent.TYPE, navigationHandler);
    }

    public final void updateDivet() {
        final ShellAppType type = getActiveShellType();
        if (type != null) {
            doUpdateDivetPosition(type, false);
        }
    }

    public ShellAppType getActiveShellType() {
        final Iterator<Entry<ShellAppType, NavigatorButton>> it = controlsMap.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ShellAppType, NavigatorButton> entry = it.next();
            if (entry.getValue().getStyleName().contains("active")) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected void activateControl(final ShellAppType type) {
        final ShellAppType currentActive = getActiveShellType();
        if (currentActive != null) {
            controlsMap.get(currentActive).removeStyleName("active");
        }
        doUpdateDivetPosition(type, currentActive != null);
        final Widget w = controlsMap.get(type);
        w.addStyleName("active");
    }

    private void doUpdateDivetPosition(final ShellAppType type, boolean animated) {
        final Widget w = controlsMap.get(type);
        int divetPos = w.getAbsoluteLeft() + (w.getOffsetWidth() / 2) - divetWrapper.getOffsetWidth() / 2;
        divet.setVisible(true);
        switch (type) {
            case APPLAUNCHER :
                divet.setResource(VShellImageBundle.BUNDLE.getDivetGreen());
                break;
            default :
                divet.setResource(VShellImageBundle.BUNDLE.getDivetWhite());
                break;
        }
        if (animated) {
            final AnimationSettings settings = new AnimationSettings();
            settings.setProperty("left", divetPos);
            JQueryWrapper.select(divetWrapper).animate(DIVET_ANIMATION_SPEED, settings);
        } else {
            divetWrapper.getStyle().setLeft(divetPos, Unit.PX);
        }

    }

    public void deactivateControls() {
        divet.setVisible(false);
        for (final ShellAppType appType : ShellAppType.values()) {
            controlsMap.get(appType).removeStyleName("active");
        }
    }

    public int getExpandedHeight() {
        return expandedHeight;
    }

    public ShellAppType getNextShellAppType() {
        final ShellAppType cur = getActiveShellType();
        if (cur != null) {
            final List<ShellAppType> values = Arrays.asList(ShellAppType.values());
            return values.get((values.indexOf(cur) + 1) % values.size());
        }
        return ShellAppType.APPLAUNCHER;
    }

    public void setIndication(ShellAppType type, int indication) {
        controlsMap.get(type).setIndication(indication);
    }
}
