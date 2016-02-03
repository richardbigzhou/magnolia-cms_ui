/**
 * This file Copyright (c) 2011-2016 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.ShellState;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VInfoMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellErrorMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VWarningMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.JQueryAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import java.util.EnumMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 */
public class MagnoliaShellViewImpl extends TouchPanel implements MagnoliaShellView {

    public static final String CLASS_NAME = "v-magnolia-shell";

    public static final String VIEWPORT_SLOT_CLASS_NAME = "v-shell-viewport-slot";

    private final Map<ViewportType, ViewportWidget> viewports = new EnumMap<ViewportType, ViewportWidget>(ViewportType.class);

    private final ShellAppLauncher mainAppLauncher;

    private ShellMessageWidget lowPriorityMessage;

    private ShellMessageWidget hiPriorityMessage;

    private Presenter presenter;

    private JQueryAnimation viewportShifter = new JQueryAnimation();

    private final Element viewportSlot = DOM.createDiv();

    // To remove focus (blur) all input elements, one just needs to focus this element.
    private final FocusPanel blurHelper = new FocusPanel();

    public MagnoliaShellViewImpl() {
        super();
        this.mainAppLauncher = new ShellAppLauncher();
        getElement().setClassName(CLASS_NAME);
        viewportSlot.setClassName(VIEWPORT_SLOT_CLASS_NAME);
        add(mainAppLauncher, getElement());


        getElement().appendChild(viewportSlot);
        viewportShifter.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                presenter.updateViewportLayout(appViewport());
            }
        });

        initKeyboardShortcutSupport();
    }


    /**
     * Bind keyboard handlers.
     * These commands are only processed if an input area does not have focus.
     */
    protected void initKeyboardShortcutSupport() {

        add(blurHelper, getElement());

        KeyPressHandler keyboardShortcutHandler = new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {

                // Only process keyboard shortcuts if user is not in an input field.
                if (isFocusedElementAnInputField()) {
                    return;
                }

                // Only process if no modifier keys are held down to avoid collision with OS or Browser hotkeys.
                if (event.isAnyModifierKeyDown()) {
                    return;
                }

                if (isFocusedElementGroovyConsole()) {
                    return;
                }

                char c = event.getCharCode();

                switch (c) {

                // Shell Apps
                case '1':
                    mainAppLauncher.toggleShellApp(ShellAppType.APPLAUNCHER);
                    break;
                case '2':
                    mainAppLauncher.toggleShellApp(ShellAppType.PULSE);
                    break;
                case '3':
                    mainAppLauncher.toggleShellApp(ShellAppType.FAVORITE);
                    break;

                // App Stack Navigation.
                case '9':
                    // We have more than one app open
                    if (appViewport().readyForAppSwipeOrShortcutNavigation()) {
                        ShellState.get().setAppStarting();
                        appViewport().goToPreviousApp();
                    }
                    break;
                case '0':
                    // We have more than one app open
                    if (appViewport().readyForAppSwipeOrShortcutNavigation()) {
                        ShellState.get().setAppStarting();
                        appViewport().goToNextApp();
                    }
                    break;

                default:
                    // Nothing
                }
            }
        };
        RootPanel.get().addDomHandler(keyboardShortcutHandler, KeyPressEvent.getType());


        /**
         * Pressing the escape key causes all elements to loose focus.
         * This is a handy way to be able to start using the single-key keyboard shortcuts.
         */
        KeyPressHandler escapeKeyPressHandler = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                int code = event.getNativeEvent().getKeyCode();
                if (code == KeyCodes.KEY_ESCAPE) {
                    blurHelper.getElement().focus();
                }
            }
        };
        RootPanel.get().addDomHandler(escapeKeyPressHandler, KeyPressEvent.getType());
    }

    /**
     * Returns whether the currently focused element is one that accepts keyboard input.
     */
    protected boolean isFocusedElementAnInputField() {
        Element focused = elementInFocus(RootPanel.get().getElement());
        String tagName = focused.getTagName();

        if ("input".equalsIgnoreCase(tagName) || "select".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName)) {
            return true;
        }
        return false;
    }

    protected native Element elementInFocus(Element element) /*-{
        return element.ownerDocument.activeElement;
    }-*/;


    protected AppsViewportWidget appViewport() {
        return (AppsViewportWidget) viewports.get(ViewportType.APP);
    }

    protected void replaceWidget(final Widget oldWidget, final Widget newWidget) {
        if (oldWidget != newWidget) {
            if (oldWidget != null) {
                remove(oldWidget);
            }
        }
        if (getWidgetIndex(newWidget) < 0) {
            add(newWidget, viewportSlot);
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        mainAppLauncher.setListener(presenter);
    }

    @Override
    public void showMessage(MessageType type, String topic, String message, String id) {
        final ShellMessageWidget msg;
        switch (type) {
        case WARNING:
            msg = new VWarningMessage(this, topic, message, id);
            if (lowPriorityMessage != null && getWidgetIndex(lowPriorityMessage) != -1) {
                lowPriorityMessage.hide();
            }
            lowPriorityMessage = msg;
            break;
        case INFO:
            msg = new VInfoMessage(this, topic, message, id);
            if (lowPriorityMessage != null && getWidgetIndex(lowPriorityMessage) != -1) {
                lowPriorityMessage.hide();
            }
            lowPriorityMessage = msg;
            break;
        case ERROR:
            msg = new VShellErrorMessage(this, topic, message, id);
            if (hiPriorityMessage != null && getWidgetIndex(hiPriorityMessage) != -1) {
                hiPriorityMessage.hide();
            }
            hiPriorityMessage = msg;
            break;
        default:
            msg = null;
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (msg != null) {
                    add(msg, getElement());
                }
            }
        });
    }

    @Override
    public void hideAllMessages() {
        if (hiPriorityMessage != null && getWidgetIndex(hiPriorityMessage) != -1) {
            hiPriorityMessage.hideWithoutTransition();
        }
        if (lowPriorityMessage != null && getWidgetIndex(lowPriorityMessage) != -1) {
            lowPriorityMessage.hideWithoutTransition();
        }

        hiPriorityMessage = null;
        lowPriorityMessage = null;
    }

    @Override
    public void updateViewport(ViewportWidget viewport, ViewportType type) {
        ViewportWidget oldViewport = viewports.get(type);
        if (oldViewport != viewport) {
            replaceWidget(oldViewport, viewport);
            viewports.put(type, viewport);
        }
    }

    @Override
    public void shiftViewportsVertically(int shiftPx, boolean animated) {
        viewportShifter.setProperty("top", mainAppLauncher.getOffsetHeight() + shiftPx);
        if (shiftPx == 0 || shiftPx == 60) {
            viewportShifter.clearTopAfterThisAnimation();
        }
        viewportShifter.run(animated ? 300 : 0, viewportSlot);
    }

    @Override
    public void setShellAppIndication(ShellAppType type, int indication) {
        mainAppLauncher.setIndication(type, indication);
    }

    @Override
    public void closeMessageEager(final String id) {
        presenter.removeMessage(id);
    }

    @Override
    public void navigateToMessageDetails(String id) {
        presenter.activateShellApp(Fragment.fromString("shell:pulse:messages/" + id));
    }

    @Override
    public void updateShellDivet() {
        mainAppLauncher.updateDivet();
    }

    @Override
    public void openOverlayOnWidget(Widget overlayWidget, Widget overlayParent) {
        add(overlayWidget, overlayParent.getElement());
    }

    @Override
    public void onShellAppStarting(ShellAppType type) {
        mainAppLauncher.activateControl(type);
    }

    @Override
    public void onAppStarting() {
        mainAppLauncher.deactivateControls();
    }

    @Override
    public void setUserMenu(Widget widget) {
        mainAppLauncher.setUserMenu(widget);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        presenter.initHistory();
    }

    @Override
    public boolean hasOverlay(Widget widget) {
        return getWidgetIndex(widget) != -1;
    }

    /*
     * We need to check for groovy console element (a DIV) css classname (terminal) and skip the shell apps shortcuts. Should the need arise for a generic mechanism usable by other components too,
     * we could expose something like a special css class or better a special html5 data-* attribute. See http://jira.magnolia-cms.com/browse/MGNLGROOVY-123
     */
    private boolean isFocusedElementGroovyConsole() {
        Element focused = elementInFocus(RootPanel.get().getElement());

        String className = focused.getClassName();

        if (className.contains("terminal")) {
            return true;
        }
        return false;
    }

}
