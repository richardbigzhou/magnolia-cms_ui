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
package info.magnolia.ui.widget.dialog.gwt.client.dialoglayout;

import info.magnolia.ui.vaadin.widget.tabsheet.client.TabSetChangedEvent;
import info.magnolia.ui.vaadin.widget.tabsheet.client.TabSetChangedEvent.Handler;
import info.magnolia.ui.vaadin.widget.tabsheet.client.VMagnoliaTab;
import info.magnolia.ui.vaadin.widget.tabsheet.client.VMagnoliaTabSheet;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.ActiveTabChangedEvent;
import info.magnolia.ui.widget.dialog.gwt.client.VDialogHeader.VDialogHeaderCallback;
import info.magnolia.ui.widget.dialog.gwt.client.VDialogTab;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.Util;

/**
 * {@link VFormDialogViewImpl}. Implements {@link VFormDialogView}.
 *
 */
public class VFormDialogViewImpl extends VBaseDialogViewImpl implements VFormDialogView {
    private VMagnoliaTabSheet tabSheet = null;
    
    private static final String CLASSNAME_CONTENT_SHOW_ALL = "show-all";

    private final List<VDialogTab> dialogTabs = new ArrayList<VDialogTab>();

    private DialogFieldWrapper lastShownProblematicField = null;

    private EventBus eventBus;

    private FocusHandler problematicFieldFocusHandler = new FocusHandler() {

        @Override
        public void onFocus(FocusEvent event) {
            final Element target = event.getRelativeElement().cast();
            final DialogFieldWrapper field = Util.findWidget(target, DialogFieldWrapper.class);
            if (field != null) {
                lastShownProblematicField = null;
                final VMagnoliaTab activeTab =  tabSheet.getActiveTab();
                if (activeTab instanceof VDialogTab) {
                    List<DialogFieldWrapper> fields = ((VDialogTab)activeTab).getFields();
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
        }
    };

    @Override
    public void setContent(Widget contentWidget) {
        if (contentWidget instanceof VMagnoliaTabSheet) { 
            this.tabSheet = (VMagnoliaTabSheet)contentWidget;
            tabSheet.addTabSetChangedHandlers(new Handler() {
                @Override
                public void onTabSetChanged(TabSetChangedEvent event) {
                    final List<VMagnoliaTab> tabs = event.getTabSheet().getTabs();
                    dialogTabs.clear();
                    for (final VMagnoliaTab tab : tabs) {
                        if (tab instanceof VDialogTab) {
                            dialogTabs.add((VDialogTab) tab);
                            final List<DialogFieldWrapper> fields = ((VDialogTab) tab).getFields();
                            for (final DialogFieldWrapper field : fields) {
                                field.addFocusHandler(problematicFieldFocusHandler);
                            }
                        }
                    }
                }
            });
            super.setContent(contentWidget);
        }
    }
    
    @Override
    public void onValidationChanged(ValidationChangedEvent event) {
        int totalProblematicFields = 0;
        for (final VDialogTab tab : dialogTabs) {
            totalProblematicFields += tab.getErrorAmount();
            final VDialogTab dialogTab = (VDialogTab) tab;
            for (final DialogFieldWrapper field : dialogTab.getFields()) {
                field.addFocusHandler(problematicFieldFocusHandler);
            }
        }
        getHeader().setErrorAmount(totalProblematicFields);
    }
    
    @Override
    protected VDialogHeaderCallback createHeaderCallback() {
        return new VDialogHeaderCallback() {

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
                VDialogTab activeTab = (VDialogTab)tabSheet.getActiveTab();
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
                        final List<VMagnoliaTab> tabs = tabSheet.getTabs();
                        int tabIndex = tabs.indexOf(activeTab);
                        for (int i = 0; i < tabs.size() - 1; ++i) {
                            final VDialogTab nextTab = (VDialogTab)tabs.get(++tabIndex % tabs.size());
                            if (nextTab.getProblematicFields().size() > 0) {
                                eventBus.fireEvent(new ActiveTabChangedEvent(nextTab));
                                lastShownProblematicField = null;
                                jumpToNextError();
                            }
                        }
                    }
                }
            }
        };
    };

    void setDescriptionVisible(boolean isVisible) {
        if (dialogTabs != null) {
            for (final VDialogTab tab : dialogTabs) {
                tab.setDescriptionVisible(isVisible);
            }   
        }
    }


    /*@Override
    public void setActiveTab(VMagnoliaTab tab) {
        lastShownProblematicField = null;
        impl.setActiveTab(tab);
        content.removeClassName(CLASSNAME_CONTENT_SHOW_ALL);
    }*/

    /*@Override
    public void showAllTabContents(boolean visible) {
        impl.showAllTabContents(visible);
        content.addClassName(CLASSNAME_CONTENT_SHOW_ALL);
    }*/

    private void scrollTo(final DialogFieldWrapper field) {
        final int top = JQueryWrapper.select(field).position().top();
        JQueryWrapper.select(tabSheet).children("v-shell-tabsheet-scroller").animate(500, new AnimationSettings() {
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
