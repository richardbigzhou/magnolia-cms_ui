/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.Util;

/**
 * Actual client side implementation of the form view. Provides the methods for
 * the client side presenter {@link com.vaadin.client.ui.form.FormConnector}.
 */
public class FormViewImpl extends FlowPanel implements FormView, ValidationChangedEvent.Handler {

    private static final String CLASSNAME = "form-panel";

    private static final String CLASSNAME_CONTENT = "form-content";

    private static final String CLASSNAME_FOOTER = "form-footer";

    private static final String CLASSNAME_FOOTER_TOOLBAR = "form-footer-toolbar";

    private static final String CLASSNAME_BUTTON = "btn-form";

    private static final String CLASSNAME_CONTENT_SHOW_ALL = "show-all";

    private static final String ClASSNAME_ERROR = "form-error";

    private final Map<String, Button> actionMap = new HashMap<String, Button>();

    private final List<FormTabWidget> formTabs = new ArrayList<FormTabWidget>();

    private final Map<FormTabWidget, Integer> errorAmounts = new HashMap<FormTabWidget, Integer>();

    private final Element contentEl = DOM.createDiv();

    private final Element footerEl = DOM.createDiv();
    private final Element footerToolbarEl = DOM.createDiv();

    private FormFieldWrapper lastFocused = null;

    private MagnoliaTabSheetView tabSheet;

    private Presenter presenter;

    private Widget footerToolbar;

    private FlowPanel errorPanel;


    private final FormHeaderWidget formHeader = new FormHeaderWidget(new FormHeaderWidget.FormHeaderCallback() {

        @Override
        public void onDescriptionVisibilityChanged(boolean isVisible) {
            setDescriptionVisible(isVisible);
            if (presenter != null) {
                presenter.runLayout();
            }
        }

    });

    public FormViewImpl() {
        super();
        setStylePrimaryName(CLASSNAME);

        contentEl.addClassName(CLASSNAME_CONTENT);
        add(formHeader);
        getElement().appendChild(contentEl);

        footerEl.addClassName(CLASSNAME_FOOTER);
        getElement().appendChild(footerEl);
        footerEl.appendChild(footerToolbarEl);
        footerToolbarEl.addClassName(CLASSNAME_FOOTER_TOOLBAR);

        errorPanel = new FlowPanel();
        errorPanel.addStyleName(ClASSNAME_ERROR);
        add(errorPanel);
        errorPanel.setVisible(false);
    }

    @Override
    public void setContent(Widget contentWidget) {
        if (contentWidget instanceof MagnoliaTabSheetView) {
            if (tabSheet != null) {
                remove(tabSheet);
            }

            this.tabSheet = (MagnoliaTabSheetView) contentWidget;
            tabSheet.addTabSetChangedHandler(new TabSetChangedEvent.Handler() {
                @Override
                public void onTabSetChanged(TabSetChangedEvent event) {
                    final List<MagnoliaTabWidget> tabs = event.getTabSheet().getTabs();
                    formTabs.clear();
                    for (final MagnoliaTabWidget tab : tabs) {
                        if (tab instanceof FormTabWidget) {
                            formTabs.add((FormTabWidget) tab);
                            ((FormTabWidget) tab).addValidationChangeHandler(FormViewImpl.this);
                        }
                    }
                }
            });

            tabSheet.addActiveTabChangedHandler(new ActiveTabChangedEvent.Handler() {
                @Override
                public void onActiveTabChanged(ActiveTabChangedEvent event) {
                    lastFocused = null;
                    if (!event.isShowingAllTabs()) {
                        contentEl.removeClassName(CLASSNAME_CONTENT_SHOW_ALL);
                        setFieldFocusHandler((FormTabWidget) event.getTab());
                    } else {
                        contentEl.addClassName(CLASSNAME_CONTENT_SHOW_ALL);
                        for (FormTabWidget tab : formTabs) {
                            setFieldFocusHandler(tab);
                        }
                    }

                }

                private void setFieldFocusHandler(FormTabWidget tab) {
                    final List<FormFieldWrapper> fields = tab.getFields();
                    for (final FormFieldWrapper field : fields) {
                        field.addFocusHandler(new FocusHandler() {
                            @Override
                            public void onFocus(FocusEvent event) {
                                final Element target = event.getRelativeElement().cast();
                                lastFocused = Util.findWidget(target, FormFieldWrapper.class);
                            }
                        });
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
            final Button button = new Button(entry.getValue());
            button.setStyleName(CLASSNAME_BUTTON);
            button.addStyleDependentName(entry.getKey());
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    getPresenter().fireAction(entry.getKey());
                }
            });
            actionMap.put(entry.getKey(), button);
            add(button, footerEl);
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


    @Override
    public void onValidationChanged(ValidationChangedEvent event) {
        errorAmounts.put(event.getSender(), event.getErrorAmount());
        Integer res = 0;
        for (Integer amount : errorAmounts.values()) {
            res += amount;
        }
        setErrorAmount(res);
    }

    public void setErrorAmount(int totalProblematicFields) {
        errorPanel.setVisible(totalProblematicFields > 0);
        if (totalProblematicFields > 0) {
            errorPanel.getElement().setInnerHTML("<span>Please correct the <b>" + totalProblematicFields + " errors </b> in this form </span>");

            final HTML errorButton = new HTML("[Jump to next error]");
            errorButton.setStyleName("action-jump-to-next-error");
            DOM.sinkEvents(errorButton.getElement(), Event.MOUSEEVENTS);
            errorButton.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    jumpToNextError();
                }
            }, ClickEvent.getType());
            errorPanel.add(errorButton);
        }
    }

    public void jumpToNextError() {
        presenter.jumpToNextError(lastFocused);
    }


    @Override
    public void setCaption(String caption) {
        formHeader.setCaption(caption);
    }

    @Override
    public Element getHeaderElement() {
        return formHeader.getElement();
    }

    @Override
    public Element getContentElement() {
        return contentEl;
    }


    @Override
    public void setHeaderToolbar(Widget headerToolbarWidget) {
        formHeader.setToolbar(headerToolbarWidget);
    }


    @Override
    public void setFooterToolbar(Widget footerToolbarWidget) {
        if (footerToolbar != null) {
            remove(footerToolbar);
        }
        footerToolbar = footerToolbarWidget;
        add(footerToolbarWidget, footerToolbarEl);
    }
}
