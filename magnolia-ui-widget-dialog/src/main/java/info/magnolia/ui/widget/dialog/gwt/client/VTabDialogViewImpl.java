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

import info.magnolia.ui.vaadin.integration.widget.client.tabsheet.VShellTab;
import info.magnolia.ui.vaadin.integration.widget.client.tabsheet.VShellTabNavigator;
import info.magnolia.ui.vaadin.integration.widget.client.tabsheet.VShellTabSheetViewImpl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * VTabDialogViewImpl.
 *
 */
public class VTabDialogViewImpl extends FlowPanel implements VTabDialogView {

    private final EventBus eventBus;
    
    private Presenter presenter;
    
    private VShellTabSheetViewImpl impl;
    
    private List<VDialogTab> dialogTabs = new ArrayList<VDialogTab>();
    
    public VTabDialogViewImpl(EventBus eventBus, Presenter presenter) {
        super();
        impl = new VShellTabSheetViewImpl(eventBus, presenter);
        
        setStylePrimaryName(CLASSNAME);
        header.addClassName(CLASSNAME_HEADER);
        content.addClassName(CLASSNAME_CONTENT);
        description.setStyleName(ClASSNAME_DESCRIPTION);
        error.setStyleName(ClASSNAME_ERROR);
        footer.addClassName(CLASSNAME_FOOTER);
        close.addClassName(ClASSNAME_CLOSE);
        help.addClassName(ClASSNAME_HELP);

        header.appendChild(close);
        header.appendChild(help);

        this.eventBus = eventBus;
        getElement().appendChild(header);


        description.setVisible(false);
        error.setVisible(false);
        add(description, getElement());
        add(error, getElement());

        getElement().appendChild(content);
        getElement().appendChild(footer);


        setCaption("Edit page properties");

        addClose();
    }

    private final Element header = DOM.createDiv();
    private final Element content = DOM.createDiv();
    private final FlowPanel description = new FlowPanel();
    private final FlowPanel error = new FlowPanel();

    private final Element footer = DOM.createDiv();
    private final Element close = DOM.createDiv();
    private final Element help = DOM.createDiv();

    private static final String CLASSNAME = "dialog-panel";
    private static final String CLASSNAME_HEADER = "dialog-header";
    private static final String ClASSNAME_DESCRIPTION = "dialog-description";
    private static final String ClASSNAME_ERROR = "dialog-error";

    private static final String CLASSNAME_CONTENT = "dialog-content";
    private static final String CLASSNAME_FOOTER = "dialog-footer";
    private static final String ClASSNAME_CLOSE = "dialog-close";
    private static final String ClASSNAME_HELP = "dialog-help";

    private static final String CLASSNAME_HELPBUTTON = "btn-dialog-help";
    private static final String CLASSNAME_CLOSEBUTTON = "btn-dialog-close";
    private static final String CLASSNAME_BUTTON = "btn-dialog";

   

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
        final Label label = new Label(caption);
        add(label, header);
    }

    public void addClose() {
        final Button closeButton = new Button();
        closeButton.setStyleName(CLASSNAME_CLOSEBUTTON);
        closeButton.addStyleName("green");
        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final com.google.gwt.event.dom.client.ClickEvent event) {
                getPresenter().closeDialog();
            }

        });
        add(closeButton, close);

    }

    @Override
    public void setDescription(final String dialogDescription) {

        final Button helpButton = new Button();

        helpButton.setStyleName(CLASSNAME_HELPBUTTON);
        helpButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                toggleDescription();
            }

        });


        final Element close = DOM.createDiv();
        close.addClassName(ClASSNAME_CLOSE);
        final Button closeButton = new Button();
        closeButton.setStyleName(CLASSNAME_CLOSEBUTTON);
        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final com.google.gwt.event.dom.client.ClickEvent event) {
                toggleDescription();
            }

        });

        add(closeButton, close);
        description.getElement().appendChild(close);

        final Element content = DOM.createSpan();
        content.setInnerText(dialogDescription);
        description.getElement().appendChild(content);


        add(helpButton, help);

    }

    void toggleDescription() {
        description.setVisible(!description.isVisible());
        presenter.notifyOfHelpAccessibilityChange(description.isVisible());
    }
    
    @Override
    public VShellTabNavigator getTabContainer() {
        return impl.getTabContainer();
    }

    @Override
    public VShellTab getTabById(String tabId) {
        return impl.getTabById(tabId);
    }

    @Override
    public List<VShellTab> getTabs() {
        return impl.getTabs();
    }

    @Override
    public void setActiveTab(VShellTab tab) {
        impl.setActiveTab(tab);
    }

    @Override
    public void removeTab(VShellTab tabToOrphan) {
        impl.removeTab(tabToOrphan);
    }

    @Override
    public void showAllTabContents(boolean visible) {
        impl.showAllTabContents(visible);
    }

    @Override
    public void addTab(VShellTab tab) {
        if (!(tab instanceof VDialogTab)) {
            throw new RuntimeException("Tab must be of VDialogTab type. You have used: " + tab.getClass());
        }
        dialogTabs.add((VDialogTab)tab);
    }
}
