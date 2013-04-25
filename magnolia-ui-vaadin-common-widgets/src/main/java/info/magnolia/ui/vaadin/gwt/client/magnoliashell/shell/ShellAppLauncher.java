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

import info.magnolia.ui.vaadin.gwt.client.icon.widget.BadgeIconWidget;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppActivatedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ViewportCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.JQueryAnimation;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
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

    private JQueryAnimation divetAnimation;

    /**
     * NavigatorButton.
     */
    public class NavigatorButton extends FlowPanel {

        private final BadgeIconWidget indicator = new BadgeIconWidget();

        private final TouchDelegate delegate = new TouchDelegate(this);

        public NavigatorButton(final ShellAppType type) {
            super();
            addStyleName("btn-shell");
            Element root = getElement();
            root.setId("btn-" + type.getCssClass());
            root.addClassName("icon-" + type.getCssClass());

            indicator.setFillColor("#fff");
            indicator.setStrokeColor("#689600");
            indicator.setOutline(true);
            root.appendChild(indicator.getElement());

            DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);
            delegate.addTouchEndHandler(new TouchEndHandler() {
                @Override
                public void onTouchEnd(TouchEndEvent event) {
                    // Has user clicked on the active shell app?
                    if (type == getActiveShellType()) {
                        // if open then close it.
                        closeShellAppViewport();
                    } else {
                        // If closed, then open it.
                        navigateToShellApp(type);
                    }
                }
            });
        }

        public void setIndication(int indication) {
            indicator.setValue(indication);
        }
    }

    private final Element divetWrapper = DOM.createDiv();

    private final TouchPanel logo = new TouchPanel();

    private final Element logoImg = DOM.createImg();

    private final Image divet = new Image(VShellImageBundle.BUNDLE.getDivetGreen());

    private final Map<ShellAppType, NavigatorButton> controlsMap = new EnumMap<ShellAppType, NavigatorButton>(ShellAppType.class);

    private final EventBus eventBus;

    public ShellAppLauncher(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.divetAnimation = new JQueryAnimation();
        getElement().setId(ID);
        construct();
        bindHandlers();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        getElement().getStyle().setTop(-60, Unit.PX);
        JQueryWrapper.select(getElement()).animate(250, new AnimationSettings() {{
                setProperty("top", 0);
        }});
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

    public void deactivateControls() {
        divet.setVisible(false);
        for (final ShellAppType appType : ShellAppType.values()) {
            controlsMap.get(appType).removeStyleName("active");
        }
    }

    public void setIndication(ShellAppType type, int indication) {
        controlsMap.get(type).setIndication(indication);
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

    private void navigateToShellApp(final ShellAppType type) {
        eventBus.fireEvent(new ShellAppActivatedEvent(type, ""));
    }

    private void closeShellAppViewport() {
        eventBus.fireEvent(new ViewportCloseEvent(ViewportType.SHELL_APP));
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
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Fragment f = Fragment.fromString(event.getValue());
                if (f.isShellApp()) {
                    activateControl(f.resolveShellAppType());
                }
            }
        });
    }

    private void doUpdateDivetPosition(final ShellAppType type, boolean animated) {
        Widget w = controlsMap.get(type);
        int divetPos = w.getAbsoluteLeft() + (w.getOffsetWidth() / 2) - divetWrapper.getOffsetWidth() / 2;
        divet.setVisible(true);
        ImageResource res = type == ShellAppType.APPLAUNCHER ?
                VShellImageBundle.BUNDLE.getDivetGreen() :
                VShellImageBundle.BUNDLE.getDivetWhite();
        divet.setResource(res);
        if (animated) {
            divetAnimation.setProperty("left", divetPos);
            divetAnimation.run(DIVET_ANIMATION_SPEED, divetWrapper);

        } else {
            divetWrapper.getStyle().setLeft(divetPos, Unit.PX);
        }

    }

    /**
     * TODO: Christopher Zimmermann CLZ
     * Restart the application by appending the &restartApplication querystring to the URL. This is
     * handy as the application is not totally stable yet.
     * Developer Preview feature.
     */
    private void emergencyRestartApplication() {
        String newHref = Window.Location.getPath() + "?restartApplication";
        Window.Location.assign(newHref);
    }
}
