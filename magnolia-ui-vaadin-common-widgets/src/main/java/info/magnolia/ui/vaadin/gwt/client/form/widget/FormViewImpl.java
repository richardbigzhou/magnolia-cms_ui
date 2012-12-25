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
package info.magnolia.ui.vaadin.gwt.client.form.widget;

import info.magnolia.ui.vaadin.gwt.client.form.formsection.event.ValidationChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.form.tab.widget.FormTabWidget;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabSetChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.widget.MagnoliaTabSheetView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.Util;

/**
 * Actual client side implementation of the form view.
 * Provides the methods for the client side presenter {@link com.vaadin.client.ui.form.FormConnector}.
 */
public class FormViewImpl extends FlowPanel implements FormView {

    private static final String CLASSNAME = "form-panel";

    private static final String CLASSNAME_CONTENT = "form-content";
    
    private static final String CLASSNAME_FOOTER = "form-footer";

    private static final String CLASSNAME_BUTTON = "btn-form";

    private static final String CLASSNAME_CONTENT_SHOW_ALL = "show-all";

    private final Map<String, Button> actionMap = new HashMap<String, Button>();
    
    private final List<FormTabWidget> formTabs = new ArrayList<FormTabWidget>();

    private final Element contentEl = DOM.createDiv();

    private final Element footer = DOM.createDiv();

    private FormFieldWrapper lastShownProblematicField = null;
    
    private MagnoliaTabSheetView tabSheet;

    private Presenter presenter;
    
    private final FocusHandler problematicFieldFocusHandler = new FocusHandler() {
        @Override
        public void onFocus(FocusEvent event) {
            final Element target = event.getRelativeElement().cast();
            final FormFieldWrapper field = Util.findWidget(target, FormFieldWrapper.class);
            if (field != null) {
                lastShownProblematicField = null;
                final List<FormFieldWrapper> fields = ((FormTabWidget)tabSheet.getActiveTab()).getFields();
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

    private final FormHeaderWidget formHeader = new FormHeaderWidget(new FormHeaderWidget.FormHeaderCallback() {

        @Override
        public void onDescriptionVisibilityChanged(boolean isVisible) {
            setDescriptionVisible(isVisible);
            if (presenter != null) {
                presenter.runLayout();   
            }
        }

        @Override
        public void jumpToNextError() {
            FormTabWidget activeTab = (FormTabWidget)tabSheet.getActiveTab();
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
                    final List<MagnoliaTabWidget> tabs = tabSheet.getTabs();
                    int tabIndex = tabs.indexOf(activeTab);
                    for (int i = 0; i < tabs.size() - 1; ++i) {
                        final FormTabWidget nextTab = (FormTabWidget)tabs.get(++tabIndex % tabs.size());
                        if (nextTab.getProblematicFields().size() > 0) {
                            //tabSheet.getEventBus().fireEvent(new ActiveTabChangedEvent(nextTab));
                            lastShownProblematicField = null;
                            jumpToNextError();
                            break;
                        }
                    }
                }
            }
        }
    });

    public FormViewImpl() {
        super();
        setStylePrimaryName(CLASSNAME);
        footer.addClassName(CLASSNAME_FOOTER);
        contentEl.addClassName(CLASSNAME_CONTENT);
        add(formHeader);
        getElement().appendChild(contentEl);
        getElement().appendChild(footer);
    }

    @Override
    public void setContent(Widget contentWidget) {
        if (contentWidget instanceof MagnoliaTabSheetView) {
            if (tabSheet != null) {
                remove(tabSheet);
            }

            this.tabSheet = (MagnoliaTabSheetView)contentWidget;
            tabSheet.addTabSetChangedHandlers(new TabSetChangedEvent.Handler() {
                @Override
                public void onTabSetChanged(TabSetChangedEvent event) {
                    final List<MagnoliaTabWidget> tabs = event.getTabSheet().getTabs();
                    formTabs.clear();
                    for (final MagnoliaTabWidget tab : tabs) {
                        if (tab instanceof FormTabWidget) {
                            formTabs.add((FormTabWidget) tab);
                            ((FormTabWidget)tab).addValidationChangeHandler(FormViewImpl.this);
                            final List<FormFieldWrapper> fields = ((FormTabWidget) tab).getFields();
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
            add(tabSheet.asWidget(), contentEl);
        }
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void setActions(Map<String, String> actions) {
        for (final Button actionButton : this.actionMap.values()) {
            remove(actionButton);
        }
        actionMap.clear();
        final Iterator<Entry<String, String>> it = actions.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, String> entry = it.next();
                final Button button =  new Button(entry.getValue());
                button.setStyleName(CLASSNAME_BUTTON);
                button.addStyleDependentName(entry.getKey());
                button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        getPresenter().fireAction(entry.getKey());
                    }
                });
                actionMap.put(entry.getKey(), button);
                add(button, footer);
        }
    }
    
    @Override
    public void setDescription(final String description) {
        formHeader.setDescription(description);
    }

    void setDescriptionVisible(boolean isVisible) {
        for (final FormTabWidget tab : formTabs) {
            tab.setDescriptionVisible(isVisible);
        }
    }

    private void scrollTo(final FormFieldWrapper field) {
        final int top = JQueryWrapper.select(field).position().top();
        JQueryWrapper.select(tabSheet.asWidget()).children(".v-shell-tabsheet-scroller").animate(500, new AnimationSettings() {
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
        for (final FormTabWidget tab : formTabs) {
            totalProblematicFields += tab.getErrorAmount();
            final FormTabWidget formTab = tab;
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

    @Override
    public Element getHeaderElement() {
        return formHeader.getElement();
    }

    @Override
    public Element getContentElement() {
        return contentEl;
    }
}
