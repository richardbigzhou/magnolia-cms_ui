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
package info.magnolia.ui.vaadin.gwt.client.form.widget;

import info.magnolia.ui.vaadin.gwt.client.form.tab.widget.FormTabWidget;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabSetChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.widget.MagnoliaTabSheetView;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.Util;

/**
 * Actual client side implementation of the form view. Provides the methods for
 * the client side presenter {@link com.vaadin.client.ui.form.FormConnector}.
 */
public class FormViewImpl extends FlowPanel implements FormView {

    private static final String CLASSNAME = "form-panel";

    private static final String CLASSNAME_CONTENT = "form-content";

    private static final String ClASSNAME_ERROR = "form-error";

    private final List<FormTabWidget> formTabs = new ArrayList<FormTabWidget>();

    private final Element contentEl = DOM.createDiv();

    private FormFieldWrapper lastFocused = null;

    private MagnoliaTabSheetView tabSheet;

    private Presenter presenter;

    private boolean hasErrors = false;

    private FlowPanel errorPanel = new FlowPanel();

    private String errorsLabel;

    private String nextErrorLabel;

    public FormViewImpl() {
        super();
        setStylePrimaryName(CLASSNAME);

        errorPanel.addStyleName(ClASSNAME_ERROR);
        errorPanel.setVisible(false);
        add(errorPanel);

        contentEl.addClassName(CLASSNAME_CONTENT);
        getElement().appendChild(contentEl);
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
                        }
                    }
                }
            });

            tabSheet.addActiveTabChangedHandler(new ActiveTabChangedEvent.Handler() {
                @Override
                public void onActiveTabChanged(ActiveTabChangedEvent event) {
                    // Focus the first field on the form of the current tab.
                    if (!hasErrors) {
                        if (!event.isShowingAllTabs()) {
                            focusFirstFieldInTab((FormTabWidget) event.getTab());
                        } else {
                            focusFirstFieldInTab(formTabs.get(0));
                        }
                    }

                    // Keep track of last focused field by adding focus handlers to every field.
                    lastFocused = null;
                    if (!event.isShowingAllTabs()) {
                        setFieldFocusHandler((FormTabWidget) event.getTab());
                    } else {
                        for (FormTabWidget tab : formTabs) {
                            setFieldFocusHandler(tab);
                        }
                    }
                }

                private void focusFirstFieldInTab(FormTabWidget tab) {
                    FormFieldWrapper firstField = tab.getFields().get(0);
                    firstField.focusField();
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
    public void setErrorsLabel(String errorsLabel) {
        this.errorsLabel = errorsLabel;
    }

    @Override
    public void setNextErrorLabel(String nextErrorLabel) {
        this.nextErrorLabel = nextErrorLabel;
    }

    @Override
    public void setErrorAmount(int totalProblematicFields) {
        hasErrors = (totalProblematicFields > 0);

        errorPanel.setVisible(totalProblematicFields > 0);
        if (totalProblematicFields > 0) {
            String formattedTotal = String.valueOf(totalProblematicFields);
            errorPanel.getElement().setInnerHTML("<span>" + errorsLabel.replaceFirst("#", formattedTotal) + "</span>");
            final HTML errorButton = new HTML("[" + nextErrorLabel + "]");
            errorButton.setStyleName("action-jump-to-next-error");
            DOM.sinkEvents(errorButton.getElement(), Event.MOUSEEVENTS);
            errorButton.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    jumpToNextError();
                }
            }, ClickEvent.getType());
            errorPanel.add(errorButton);
            presenter.onErrorsDisplayed();
        }
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDescriptionVisible(boolean isVisible) {
        for (final FormTabWidget tab : formTabs) {
            tab.setDescriptionVisible(isVisible);
        }
    }

    public void jumpToNextError() {
        presenter.jumpToNextError(lastFocused);
    }

    @Override
    public void setMaxHeight(int height) {
        if (this.hasErrors) {
            height -= JQueryWrapper.select(errorPanel).marginHeight();
        }
        if (this.tabSheet != null) {
            this.tabSheet.setMaxHeight(height);
        }
    }
}
