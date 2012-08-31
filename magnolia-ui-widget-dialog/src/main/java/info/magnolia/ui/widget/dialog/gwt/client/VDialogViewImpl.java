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
package info.magnolia.ui.widget.dialog.gwt.client;

import info.magnolia.ui.vaadin.widget.tabsheet.client.VMagnoliaTab;
import info.magnolia.ui.vaadin.widget.tabsheet.client.VMagnoliaTabNavigator;
import info.magnolia.ui.vaadin.widget.tabsheet.client.VMagnoliaTabSheetViewImpl;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.ActiveTabChangedEvent;
import info.magnolia.ui.widget.dialog.gwt.client.VDialogHeader.VDialogHeaderCallback;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.DialogFieldWrapper;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.MGWT;
import com.vaadin.terminal.gwt.client.Util;

/**
 * VTabDialogViewImpl.
 */
public class VDialogViewImpl extends FlowPanel implements VDialogView {

    private static final String CLASSNAME = "dialog-panel";

    private static final String CLASSNAME_CONTENT = "dialog-content";

    private static final String CLASSNAME_FOOTER = "dialog-footer";

    private static final String CLASSNAME_BUTTON = "btn-dialog";

    private final List<VDialogTab> dialogTabs = new ArrayList<VDialogTab>();

    private final Element content = DOM.createDiv();

    private final Element footer = DOM.createDiv();

    private final VMagnoliaTabSheetViewImpl impl;

    private DialogFieldWrapper lastShownProblematicField = null;

    private EventBus eventBus;

    public boolean isAFieldFocussed; //Whether a field in the view has focus, required for iPad Keyboard closing.

    private FocusHandler problematicFieldFocusHandler = new FocusHandler() {
        @Override
        public void onFocus(FocusEvent event) {
            isAFieldFocussed = true;
            final Element target = event.getRelativeElement().cast();
            final DialogFieldWrapper field = Util.findWidget(target, DialogFieldWrapper.class);
            if (field != null) {
                lastShownProblematicField = null;
                final List<DialogFieldWrapper> fields = getActiveTab().getFields();
                int index = fields.indexOf(field);
                if (index >= 0) {
                    if (field.hasError()) {
                        lastShownProblematicField = field;
                    } else {
                        while (index > 0) {
                            --index;
                            if (fields.get(index).hasError()) {
                                lastShownProblematicField = fields.get(index);
                                break;
                            }
                        }
                    }
                }
            }
        }
    };


    /**
     * On field blur - if no element got focussed, then scroll document to top.
     * This is to fix the iOS problem where the keyboard shifts the layout up, but when the keyboard dissappears
     * it does not shift the layout all the way back down.
     * Note: blur event is triggered when keyboard is closed.
     */
    private BlurHandler iosKeyboardCloseHandler = new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
            isAFieldFocussed = false;

            Timer timer = new Timer() {
                @Override
                public void run() {
                    if (!isAFieldFocussed){
                        Document.get().getBody().setScrollTop(0);
                    }
                }
           };
           timer.schedule(10);
        }
    };


    private final VDialogHeader dialogHeader = new VDialogHeader(new VDialogHeaderCallback() {

        @Override
        public void onDescriptionVisibilityChanged(boolean isVisible) {
            setDescriptionVisible(isVisible);
        }

        @Override
        public void onCloseFired() {
            getPresenter().closeDialog();
        }

        @Override
        public void jumpToNextError() {
            VDialogTab activeTab = getActiveTab();
            final List<DialogFieldWrapper> problematicFields = activeTab.getProblematicFields();
            if (lastShownProblematicField == null && !problematicFields.isEmpty()) {
                final DialogFieldWrapper field = problematicFields.get(0);
                scrollTo(field);
                lastShownProblematicField = field;
            } else {
                int index = problematicFields.indexOf(lastShownProblematicField) + 1;
                if (index <= problematicFields.size() - 1) {
                    final DialogFieldWrapper nextField = problematicFields.get(index);
                    lastShownProblematicField = nextField;
                    scrollTo(lastShownProblematicField);
                } else {
                    final List<VMagnoliaTab> tabs = getTabs();
                    VMagnoliaTab nextTab = tabs.get((tabs.indexOf(activeTab) + 1) % tabs.size());
                    eventBus.fireEvent(new ActiveTabChangedEvent(nextTab));
                    jumpToNextError();
                }
            }
        }
    });

    private Presenter presenter;

    public VDialogViewImpl(EventBus eventBus, Presenter presenter) {
        super();
        this.eventBus = eventBus;
        this.presenter = presenter;

        impl = new VMagnoliaTabSheetViewImpl(eventBus, presenter);

        setStylePrimaryName(CLASSNAME);
        content.addClassName(CLASSNAME_CONTENT);
        footer.addClassName(CLASSNAME_FOOTER);

        add(dialogHeader);

        getElement().appendChild(content);
        getElement().appendChild(footer);

        add(impl, content);

    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public boolean hasChildComponent(final Widget component) {
        boolean isChild = false;
        for (final Widget widget : getChildren()) {
            if (component == widget) {
                isChild = true;
            }
        }
        return isChild;
    }

    @Override
    public void addAction(final String name, final String label) {
        final Button button = new Button(label);
        button.setStyleName(CLASSNAME_BUTTON);
        button.addStyleDependentName(name);
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final com.google.gwt.event.dom.client.ClickEvent event) {
                getPresenter().fireAction(name);
            }

        });
        add(button, footer);
    }

    @Override
    public void setCaption(final String caption) {
        dialogHeader.setDialogCaption(caption);
    }

    @Override
    public void setDescription(final String dialogDescription) {
        dialogHeader.setDescription(dialogDescription);
    }

    void setDescriptionVisible(boolean isVisible) {
        for (final VDialogTab tab : dialogTabs) {
            tab.setDescriptionVisible(isVisible);
        }
    }

    @Override
    public VMagnoliaTabNavigator getTabContainer() {
        return impl.getTabContainer();
    }

    @Override
    public VMagnoliaTab getTabById(String tabId) {
        return impl.getTabById(tabId);
    }

    @Override
    public List<VMagnoliaTab> getTabs() {
        return impl.getTabs();
    }

    @Override
    public void setActiveTab(VMagnoliaTab tab) {
        lastShownProblematicField = null;
        impl.setActiveTab(tab);
    }

    @Override
    public void removeTab(VMagnoliaTab tabToOrphan) {
        impl.removeTab(tabToOrphan);
    }

    @Override
    public void showAllTabContents(boolean visible) {
        impl.showAllTabContents(visible);
    }

    @Override
    public HandlerRegistration addScrollHandler(ScrollHandler handler) {
        return impl.addScrollHandler(handler);
    }

    @Override
    public void addTab(VMagnoliaTab tab) {
        if (!(tab instanceof VDialogTab)) {
            throw new RuntimeException("Tab must be of VDialogTab type. You have used: " + tab.getClass());
        }
        dialogTabs.add((VDialogTab) tab);
        impl.addTab(tab);
        final List<DialogFieldWrapper> fields = ((VDialogTab) tab).getFields();
        for (final DialogFieldWrapper field : fields) {
            field.addFocusHandler(problematicFieldFocusHandler);
            if (!MGWT.getOsDetection().isDesktop()){
                //On iOS, shift page down when keyboard is closed.
                field.addBlurHandler(iosKeyboardCloseHandler);
            }
        }
    }

    @Override
    public void recalculateErrors() {
        int totalProblematicFields = 0;
        for (final VDialogTab tab : dialogTabs) {
            totalProblematicFields += tab.getErorAmount();
            final VDialogTab dialogTab = (VDialogTab) tab;
            for (final DialogFieldWrapper field : dialogTab.getFields()) {
                field.addFocusHandler(problematicFieldFocusHandler);
                if (!MGWT.getOsDetection().isDesktop()){
                    //On iOS, shift page down when keyboard is closed.
                    field.addBlurHandler(iosKeyboardCloseHandler);
                }
            }
        }
        dialogHeader.setErrorAmount(totalProblematicFields);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        int heightPx = JQueryWrapper.parseInt(height);
        impl.setHeight((heightPx - dialogHeader.getOffsetHeight() - footer.getOffsetHeight()) + "px");
    }

    @Override
    public VDialogTab getActiveTab() {
        return (VDialogTab) impl.getActiveTab();
    }

    private void scrollTo(final DialogFieldWrapper field) {
        final int top = JQueryWrapper.select(field).position().top();
        JQueryWrapper.select(getScroller()).animate(300, new AnimationSettings() {
            {
                setProperty("scrollTop", top - 30);
                addCallback(new JQueryCallback() {
                    @Override
                    public void execute(JQueryWrapper query) {
                        field.focusField();
                    }
                });
            }
        });
    }

    @Override
    public Widget getScroller() {
        return impl.getScroller();
    }

    @Override
    public void setShowActiveTabFullscreen(boolean isFullscreen) {
        impl.setShowActiveTabFullscreen(isFullscreen);
    }

    @Override
    public int getTabHeight(VMagnoliaTab tab) {
        return impl.getTabHeight(tab);
    }
}
