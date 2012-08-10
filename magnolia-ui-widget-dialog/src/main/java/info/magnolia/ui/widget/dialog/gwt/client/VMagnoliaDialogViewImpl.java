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
import info.magnolia.ui.widget.dialog.gwt.client.VMagnoliaDialogHeader.VDialogHeaderCallback;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.DialogFieldWrapper;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * VTabDialogViewImpl.
 */
public class VMagnoliaDialogViewImpl extends FlowPanel implements VMagnoliaDialogView {

    private static final String CLASSNAME = "dialog-panel";

    private static final String CLASSNAME_CONTENT = "dialog-content";

    private static final String CLASSNAME_FOOTER = "dialog-footer";

    private static final String CLASSNAME_BUTTON = "btn-dialog";

    private final List<VMagnoliaDialogTab> dialogTabs = new ArrayList<VMagnoliaDialogTab>();

    private final Element content = DOM.createDiv();

    private final Element footer = DOM.createDiv();

    private final VMagnoliaTabSheetViewImpl impl;

    private DialogFieldWrapper lastShownProblematicField = null;

    private EventBus eventBus;
    
    private final VMagnoliaDialogHeader dialogHeader = new VMagnoliaDialogHeader(new VDialogHeaderCallback() {

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
            VMagnoliaDialogTab activeTab = getActiveTab();
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
                    eventBus.fireEvent(new ActiveTabChangedEvent(tabs.get((getTabs().indexOf(getActiveTab()) + 1) % getTabs().size())));
                    jumpToNextError();
                }
            }
        }
    });

    private Presenter presenter;

    public VMagnoliaDialogViewImpl(EventBus eventBus, Presenter presenter) {
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
        
        setCaption("Edit page properties");

        addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                VConsole.log("jsjdlkajslkdjaslkdjalskdjaslkdjasldjaskl");
            }
        });
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

    void setCaption(final String caption) {
        dialogHeader.setDialogCaption(caption);
    }

    @Override
    public void setDescription(final String dialogDescription) {
        dialogHeader.setDescription(dialogDescription);
    }

    void setDescriptionVisible(boolean isVisible) {
        for (final VMagnoliaDialogTab tab : dialogTabs) {
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
        if (!(tab instanceof VMagnoliaDialogTab)) {
            throw new RuntimeException("Tab must be of VDialogTab type. You have used: " + tab.getClass());
        }
        dialogTabs.add((VMagnoliaDialogTab) tab);
        impl.addTab(tab);
    }

    @Override
    public void recalculateErrors() {
        int totalProblematicFields = 0;
        for (final VMagnoliaDialogTab tab : dialogTabs) {
            totalProblematicFields += tab.getErorAmount();
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
    public VMagnoliaDialogTab getActiveTab() {
        return (VMagnoliaDialogTab) impl.getActiveTab();
    }

    private void scrollTo(final DialogFieldWrapper field) {
        final int top = JQueryWrapper.select(field).position().top();
        final ScrollPanel scroller = Util.findWidget(field.getElement(), ScrollPanel.class);
        JQueryWrapper.select(scroller).animate(300, new AnimationSettings() {
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
}
