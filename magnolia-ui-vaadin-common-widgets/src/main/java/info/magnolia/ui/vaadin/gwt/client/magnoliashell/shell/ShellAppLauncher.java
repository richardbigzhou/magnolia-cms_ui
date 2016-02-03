/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.JQueryAnimation;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;
import com.vaadin.client.BrowserInfo;

/**
 * Navigation bar.
 */
public class ShellAppLauncher extends FlowPanel {

    /**
     * Listener.
     */
    public interface Listener {

        void onHideShellAppsRequested();

        void showShellApp(ShellAppType type);
    }

    private final static int DIVET_ANIMATION_SPEED = 400;

    public static final String USER_MENU_CLASS_NAME = "v-shell-user-menu-wrapper";

    private final static String ID = "main-launcher";

    private final Element divetWrapper = DOM.createDiv();

    private final TouchPanel logo = new TouchPanel();

    private final Element userMenu = DOM.createDiv();

    private final Element logoImg = DOM.createImg();

    private final Map<ShellAppType, NavigatorButton> controlsMap = new EnumMap<ShellAppType, NavigatorButton>(ShellAppType.class);

    private JQueryAnimation divetAnimation;

    private Listener listener;

    public ShellAppLauncher() {
        super();
        this.divetAnimation = new JQueryAnimation();
        getElement().setId(ID);
        construct();
        bindHandlers();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                getElement().getStyle().setTop(-60, Unit.PX);
                JQueryWrapper.select(getElement()).animate(250, new AnimationSettings() {
                    {
                        setProperty("top", 0);
                    }
                });
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public final void updateDivet() {
        final ShellAppType type = getActiveShellType();
        if (type != null) {
            doUpdateDivetPosition(type, false);
        }
    }

    public void setUserMenu(Widget widget) {
        add(widget, userMenu);
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

    public void deactivateControls() {
        divetWrapper.getStyle().setDisplay(Display.NONE);
        for (final NavigatorButton button : controlsMap.values()) {
            button.removeStyleName("active");
        }
    }

    public void setIndication(ShellAppType type, int indication) {
        controlsMap.get(type).setIndication(indication);
    }

    public void activateControl(final ShellAppType type) {
        final ShellAppType currentActive = getActiveShellType();
        if (currentActive != null) {
            controlsMap.get(currentActive).removeStyleName("active");
        }
        doUpdateDivetPosition(type, currentActive != null);
        final Widget w = controlsMap.get(type);
        w.addStyleName("active");
    }

    private void construct() {
        divetWrapper.setId("divet");
        logoImg.setId("logo");
        String baseUrl = GWT.getModuleBaseURL().replace("widgetsets/" + GWT.getModuleName() + "/", "");
        String logoPath = baseUrl + "themes/admincentraltheme/img/";
        boolean isIE8 = BrowserInfo.get().isIE8();
        logoImg.setAttribute("src", logoPath + (isIE8 ? "logo-magnolia.png" : "logo-magnolia.svg"));

        logo.getElement().appendChild(logoImg);
        add(logo);

        userMenu.setClassName(USER_MENU_CLASS_NAME);
        getElement().appendChild(userMenu);
        getElement().appendChild(divetWrapper);
        for (final ShellAppType appType : ShellAppType.values()) {
            final NavigatorButton w = new NavigatorButton(appType);
            w.addTouchEndHandler(new TouchEndHandler() {
                @Override
                public void onTouchEnd(TouchEndEvent event) {
                    toggleShellApp(appType);
                }
            });

            controlsMap.put(appType, w);
            add(w);
        }
        divetWrapper.getStyle().setDisplay(Display.NONE);
    }

    /**
     * Toggle the 'openness' of the specified shellApp.
     * 
     * @param appType
     */
    public void toggleShellApp(ShellAppType appType) {
        // Has user clicked on the active shell app?
        if (appType == getActiveShellType()) {
            // if open then close it.
            listener.onHideShellAppsRequested();
        } else {
            // If closed, then open it.
            listener.showShellApp(appType);
        }
    }

    private void bindHandlers() {
        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);
    }

    private void doUpdateDivetPosition(final ShellAppType type, boolean animated) {
        Widget w = controlsMap.get(type);
        divetWrapper.getStyle().setDisplay(Display.BLOCK);

        switch (type) {
        case APPLAUNCHER:
            divetWrapper.setClassName("divet-green");
            break;
        case PULSE:
            divetWrapper.setClassName("divet-gray");
            break;
        case FAVORITE:
            divetWrapper.setClassName("divet-white");
            break;
        default:
            divetWrapper.setClassName("divet-white");
        }

        int divetPos = w.getAbsoluteLeft() + (w.getOffsetWidth() / 2) - divetWrapper.getOffsetWidth() / 2;
        if (animated && divetWrapper.getAbsoluteLeft() != divetPos) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "DIVET POS: " + divetPos);
            divetAnimation.setProperty("left", divetPos);
            divetAnimation.run(DIVET_ANIMATION_SPEED, divetWrapper);

        } else {
            divetWrapper.getStyle().setLeft(divetPos, Unit.PX);
        }

    }
}
