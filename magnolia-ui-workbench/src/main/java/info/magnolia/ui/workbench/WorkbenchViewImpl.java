/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.workbench;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.vaadin.icon.Icon;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Implementation of the workbench view.
 */
public class WorkbenchViewImpl extends VerticalLayout implements WorkbenchView, Serializable {

    private final CssLayout toolBar = new CssLayout();

    private final CssLayout viewModes = new CssLayout();

    private final CssLayout searchBox = new CssLayout();

    private TextField searchField;

    private Button clearSearchBoxButton;

    private Icon searchIcon;

    private Icon searchArrow;

    private StatusBarView statusBar;

    private Map<ViewType, ContentView> contentViews = new EnumMap<ViewType, ContentView>(ViewType.class);

    private Map<ViewType, Button> contentViewsButton = new EnumMap<ViewType, Button>(ViewType.class);

    private ViewType currentViewType = ViewType.TREE;

    /**
     * for going back from search view if search expression is empty.
     */
    private ViewType previousViewType = currentViewType;

    private final Property.ValueChangeListener searchFieldListener = new Property.ValueChangeListener() {

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            listener.onSearch(searchField.getValue().toString());

            boolean hasSearchContent = !searchField.getValue().isEmpty();
            if (hasSearchContent) {
                searchBox.addStyleName("has-content");
            } else {
                searchBox.removeStyleName("has-content");
            }
            searchField.focus();
        }
    };

    private WorkbenchView.Listener listener;

    public WorkbenchViewImpl(){

        setSizeFull();
        setMargin(new MarginInfo(true, false, false, true));
        addStyleName("workbench");

        viewModes.setStyleName("view-modes");

        clearSearchBoxButton = new Button();
        clearSearchBoxButton.setStyleName("m-closebutton");
        clearSearchBoxButton.addStyleName("icon-delete-search");
        clearSearchBoxButton.addStyleName("searchbox-clearbutton");
        clearSearchBoxButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                searchField.setValue("");
            }
        });

        searchIcon = new Icon("search");
        searchIcon.addStyleName("searchbox-icon");

        searchArrow = new Icon("arrow2_s");
        searchArrow.addStyleName("searchbox-arrow");

        searchField = buildSearchField();

        searchBox.setVisible(false);
        searchBox.addComponent(searchField);
        searchBox.addComponent(clearSearchBoxButton);
        searchBox.addComponent(searchIcon);
        searchBox.addComponent(searchArrow);
        searchBox.setStyleName("searchbox");

        toolBar.addStyleName("toolbar");
        toolBar.setWidth(100, Unit.PERCENTAGE);
        toolBar.addComponent(viewModes);
        toolBar.addComponent(searchBox);

        addComponent(toolBar);
        setExpandRatio(toolBar, 0);


    }

    @Override
    public void setSearchQuery(String query) {
        if (searchField == null) {
            return;
        }
        // turn off value change listener, so that presenter does not think there was user input and searches again
        searchField.removeValueChangeListener(searchFieldListener);
        if (StringUtils.isNotBlank(query)) {
            searchField.setValue(query);
            searchField.focus();
        } else {
            searchField.setValue("");
            searchBox.removeStyleName("has-content");
        }
        searchField.addValueChangeListener(searchFieldListener);

    }

    @Override
    public void addContentView(ViewType viewType, ContentView view, ContentPresenterDefinition contentViewDefintion) {
        contentViews.put(viewType, view);

        if (viewType.equals(ViewType.SEARCH)) {
            // do not add a button for search
            return;
        }
        if (viewType.equals(ViewType.LIST)) {
            searchBox.setVisible(true);
        }

        // set button
        Button button = buildButton(viewType, contentViewDefintion.getIcon(), contentViewDefintion.isActive());
        contentViewsButton.put(viewType, button);
        viewModes.addComponent(button);
        // set active
        if (contentViewDefintion.isActive()) {
            currentViewType = previousViewType = viewType;
        }
    }

    @Override
    public void setViewType(ViewType type) {
        removeComponent(getSelectedView().asVaadinComponent());
        final Component c = contentViews.get(type).asVaadinComponent();
        addComponent(c, 1); // between tool bar and status bar
        setExpandRatio(c, 1);

        if (type != ViewType.SEARCH) {
            previousViewType = type;
            setSearchQuery(null);
        }
        setViewTypeStyling(type);

        currentViewType = type;
    }

    private void fireViewTypeChangedEvent(ViewType viewType) {
        this.listener.onViewTypeChanged(viewType);
    }

    @Override
    public void setStatusBarView(StatusBarView statusBar) {
        Component c = statusBar.asVaadinComponent();
        if (this.statusBar == null) {
            addComponent(c, getComponentCount()); // add last
        } else {
            replaceComponent(this.statusBar.asVaadinComponent(), c);
        }
        setExpandRatio(c, 0);
        this.statusBar = statusBar;
    }

    @Override
    public ContentView getSelectedView() {
        return contentViews.get(currentViewType);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setListener(WorkbenchView.Listener listener) {
        this.listener = listener;
    }

    private Button buildButton(final ViewType viewType, final String icon, final boolean active) {
        NativeButton button = new NativeButton(null, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fireViewTypeChangedEvent(viewType);
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);

        button.setHtmlContentAllowed(true);
        button.setCaption("<span class=\"" + icon + "\"></span><span class=\"view-type-arrow view-type-arrow-" + viewType.getText() + " icon-arrow2_n\"></span>");

        if (active) {
            button.addStyleName("active");
        }
        return button;
    }

    private void setViewTypeStyling(final ViewType viewType) {
        for (Map.Entry<ViewType, Button> entry : contentViewsButton.entrySet()) {
            entry.getValue().removeStyleName("active");
            if (entry.getKey().equals(viewType)) {
                entry.getValue().addStyleName("active");
            }
        }
        // search is a list view
        if (viewType.equals(ContentView.ViewType.SEARCH) && contentViewsButton.containsKey(ContentView.ViewType.LIST)) {
            contentViewsButton.get(ContentView.ViewType.LIST).addStyleName("active");
        }
    }

    private TextField buildSearchField() {
        final TextField field = new TextField();
        final String inputPrompt = MessagesUtil.getWithDefault("toolbar.search.prompt", "Search");

        field.setInputPrompt(inputPrompt);
        field.setSizeUndefined();
        field.addStyleName("searchfield");

        // TextField has to be immediate to fire value changes when pressing Enter, avoiding ShortcutListener overkill.
        field.setImmediate(true);
        field.addListener(searchFieldListener);

        field.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                // put the cursor at the end of the field
                TextField tf = (TextField) event.getSource();
                tf.setCursorPosition(tf.getValue().length());
            }
        });

        // No blur handler.

        return field;
    }

    @Override
    public void setMultiselect(boolean multiselect) {
        for (ViewType type : contentViews.keySet()) {
            contentViews.get(type).setMultiselect(multiselect);
        }
    }
}
