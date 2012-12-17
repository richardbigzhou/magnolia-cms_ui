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
package info.magnolia.ui.vaadin.gwt.client.form;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.TabSetChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.VMagnoliaTab;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.VMagnoliaTabSheet;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.Util;

/**
 * Actual client side implementation of the form view.
 * Provides the methods for the client side presenter {@link VForm}.
 */
public class VFormViewImpl extends FlowPanel implements VFormView {

    private static final String CLASSNAME = "form-panel";

    private static final String CLASSNAME_FOOTER = "form-footer";

    private static final String CLASSNAME_BUTTON = "btn-form";

    private static final String CLASSNAME_CONTENT_SHOW_ALL = "show-all";

    private final List<VFormTab> formTabs = new ArrayList<VFormTab>();

    private final Element contentEl = DOM.createDiv();

    private final Element footer = DOM.createDiv();

    private FormFieldWrapper lastShownProblematicField = null;
    
    private VMagnoliaTabSheet tabSheet;

    private final FocusHandler problematicFieldFocusHandler = new FocusHandler() {
        @Override
        public void onFocus(FocusEvent event) {
            final Element target = event.getRelativeElement().cast();
            final FormFieldWrapper field = Util.findWidget(target, FormFieldWrapper.class);
            if (field != null) {
                lastShownProblematicField = null;
                final List<FormFieldWrapper> fields = ((VFormTab)tabSheet.getActiveTab()).getFields();
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

    private final VFormHeader formHeader = new VFormHeader(new VFormHeader.VFormHeaderCallback() {

        @Override
        public void onDescriptionVisibilityChanged(boolean isVisible) {
            setDescriptionVisible(isVisible);
            if (presenter != null) {
                presenter.runLayout();   
            }
        }

        @Override
        public void jumpToNextError() {
            VFormTab activeTab = (VFormTab)tabSheet.getActiveTab();
            final List<FormFieldWrapper> problematicFields = activeTab.getProblematicFields();
            if (lastShownProblematicField == null && !problematicFields.isEmpty()) {
                final FormFieldWrapper field = problematicFields.get(0);
                scrollTo(field);
                lastShownProblematicField = field;
            } else {
                int index = problematicFields.indexOf(lastShownProblematicField) + 1;
                if (index <= problematicFields.size() - 1) {
                    final FormFieldWrapper nextField = problematicFields.get(index);
                    lastShownProblematicField = nextField;
                    scrollTo(lastShownProblematicField);
                } else {
                    final List<VMagnoliaTab> tabs = tabSheet.getTabs();
                    int tabIndex = tabs.indexOf(activeTab);
                    for (int i = 0; i < tabs.size() - 1; ++i) {
                        final VFormTab nextTab = (VFormTab)tabs.get(++tabIndex % tabs.size());
                        if (nextTab.getProblematicFields().size() > 0) {
                            tabSheet.getEventBus().fireEvent(new ActiveTabChangedEvent(nextTab));
                            lastShownProblematicField = null;
                            jumpToNextError();
                            break;
                        }
                    }
                }
            }
        }
    });

    private Presenter presenter;

    public VFormViewImpl() {
        super();
        setStylePrimaryName(CLASSNAME);
        footer.addClassName(CLASSNAME_FOOTER);
        add(formHeader);
        getElement().appendChild(contentEl);
        getElement().appendChild(footer);
    }

    @Override
    public void setContent(Widget contentWidget) {
        if (contentWidget instanceof VMagnoliaTabSheet) {
            if (tabSheet != null) {
                remove(tabSheet);
            }

            this.tabSheet = (VMagnoliaTabSheet)contentWidget;
            tabSheet.addTabSetChangedHandlers(new TabSetChangedEvent.Handler() {
                @Override
                public void onTabSetChanged(TabSetChangedEvent event) {
                    final List<VMagnoliaTab> tabs = event.getTabSheet().getTabs();
                    formTabs.clear();
                    for (final VMagnoliaTab tab : tabs) {
                        if (tab instanceof VFormTab) {
                            formTabs.add((VFormTab) tab);
                            ((VFormTab)tab).addValidationChangeHandler(VFormViewImpl.this);
                            final List<FormFieldWrapper> fields = ((VFormTab) tab).getFields();
                            for (final FormFieldWrapper field : fields) {
                                field.addFocusHandler(problematicFieldFocusHandler);
                            }
                        }
                    }
                }
            });

            tabSheet.addActiveTabChangedHandler(new ActiveTabChangedEvent.Handler() {
                @Override
                public void onActiveTabChanged(ActiveTabChangedEvent event) {
                    lastShownProblematicField = null;
                    if (!event.isShowingAllTabs()) {
                        contentEl.removeClassName(CLASSNAME_CONTENT_SHOW_ALL);
                    } else {
                        contentEl.addClassName(CLASSNAME_CONTENT_SHOW_ALL);
                    }
                }
            });
            add(tabSheet, contentEl);
        }
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public VMagnoliaTabSheet getContent() {
        return tabSheet;
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
    public void setDescription(final String description) {
        formHeader.setDescription(description);
    }

    void setDescriptionVisible(boolean isVisible) {
        for (final VFormTab tab : formTabs) {
            tab.setDescriptionVisible(isVisible);
        }
    }

    @Override
    public int getFormWidth() {
        return getOffsetWidth();
    }

    @Override
    public int getFormHeight() {
        if (getParent() != null) {
            int baseHeight = getParent().getOffsetHeight();
            int headerHeight = formHeader.getOffsetHeight();
            int footerHeight = footer.getOffsetHeight();
            return baseHeight - headerHeight - footerHeight;
        }
        return 0;
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        Integer heightPx = JQueryWrapper.parseInt(height); 
        if (tabSheet != null && heightPx != null) {
            tabSheet.setHeight((heightPx - formHeader.getOffsetHeight() - footer.getOffsetHeight()) + "px");            
        }
    }

    private void scrollTo(final FormFieldWrapper field) {
        final int top = JQueryWrapper.select(field).position().top();
        JQueryWrapper.select(tabSheet).children(".v-shell-tabsheet-scroller").animate(500, new AnimationSettings() {
            {
                setProperty("scrollTop", top - 30);
                addCallback(new JQueryCallback() {
                    @Override
                    public void execute(JQueryWrapper query) {
                        new Timer() {
                            @Override
                            public void run() {
                                field.focusField();
                            };
                        }.schedule(500);
                    }
                });
            }
        });
    }

    @Override
    public void onValidationChanged(ValidationChangedEvent event) {
        int totalProblematicFields = 0;
        for (final VFormTab tab : formTabs) {
            totalProblematicFields += tab.getErrorAmount();
            final VFormTab formTab = tab;
            for (final FormFieldWrapper field : formTab.getFields()) {
                field.addFocusHandler(problematicFieldFocusHandler);
            }
        }
        formHeader.setErrorAmount(totalProblematicFields);
    }

    @Override
    public void setCaption(String caption) {
        formHeader.setFormCaption(caption);
    }
}
