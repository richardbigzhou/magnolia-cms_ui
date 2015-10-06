/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.extension.ShortcutProtector;
import info.magnolia.ui.vaadin.icon.Icon;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.search.SearchPresenterDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;
import info.magnolia.ui.workbench.tree.TreeView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
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

    protected final Panel keyboardEventPanel;

    private TextField searchField;

    private Button clearSearchBoxButton;

    private Icon searchIcon;

    private Icon searchArrow;

    private StatusBarView statusBar;

    private Map<String, ContentView> contentViews = new HashMap<String, ContentView>();

    private Map<String, Button> contentViewsButton = new HashMap<String, Button>();

    private String currentViewType = TreePresenterDefinition.VIEW_TYPE;

    /**
     * for going back from search view if search expression is empty.
     */
    private String previousViewType = currentViewType;

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
    private final SimpleTranslator i18n;

    @Inject
    public WorkbenchViewImpl(SimpleTranslator i18n) {
        this.i18n = i18n;

        setSizeFull();
        setMargin(new MarginInfo(true, false, false, true));
        addStyleName("workbench");

        viewModes.setStyleName("view-modes");

        clearSearchBoxButton = new Button();
        clearSearchBoxButton.addStyleName("icon-delete-search");
        clearSearchBoxButton.addStyleName("searchbox-clearbutton");
        // Preventing the button to spoil the tab-navigation due to its changing display value.
        clearSearchBoxButton.setTabIndex(-1);
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

        keyboardEventPanel = new Panel();
        keyboardEventPanel.setSizeFull();
        keyboardEventPanel.addStyleName("keyboard-panel");
        addComponent(keyboardEventPanel, 1); // between tool bar and status bar
        setExpandRatio(keyboardEventPanel, 1);

        bindKeyboardHandlers();
    }

    public void bindKeyboardHandlers() {

        final ShortcutListener enterShortcut = new ShortcutListener("Enter shortcut", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                getSelectedView().onShortcutKey(ShortcutAction.KeyCode.ENTER, null);
            }
        };
        keyboardEventPanel.addShortcutListener(enterShortcut);

        final ShortcutListener deleteShortcut = new ShortcutListener("Delete shortcut", ShortcutAction.KeyCode.DELETE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                getSelectedView().onShortcutKey(ShortcutAction.KeyCode.DELETE, null);
            }
        };
        // MGNLUI-2106 disable the delete shortcut until we apply it without disrupting inplace-editing
        // keyboardEventPanel.addShortcutListener(deleteShortcut);

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


    /**
     * Adds a content view by given view type, content view and content view definition.
     * @deprecated since 5.4.3. Use addContentView( viewType, view, viewTypeIcon) instead. Interface method will become deprecated on magnolia-ui 5.5.
     */
    @Deprecated
    @Override
    public void addContentView(String viewType, ContentView view, ContentPresenterDefinition contentViewDefintion) {
        addContentView(viewType, view, contentViewDefintion.getIcon());
    }

    /**
     * Adds a content view by given view type, content view and view type icon.
     */
    public void addContentView(String viewType, ContentView view, String viewTypeIcon) {
        contentViews.put(viewType, view);

        if (view instanceof TreeView) {
            ((TreeView) view).setActionManager(keyboardEventPanel);
        }

        // display search-box only if both list and search content presenters are configured
        if (contentViews.containsKey(ListPresenterDefinition.VIEW_TYPE) && contentViews.containsKey(SearchPresenterDefinition.VIEW_TYPE)) {
            if (toolBar.getComponentCount() > 1) { // components > 1 because first component in the toolbar is switcher between tree/list view
                toolBar.getComponent(1).setVisible(true);
            }
        }

        if (SearchPresenterDefinition.VIEW_TYPE.equals(viewType)) {
            // do not add a view-type button for search
            return;
        }

        // set button
        Button button = buildButton(viewType, viewTypeIcon);
        contentViewsButton.put(viewType, button);
        viewModes.addComponent(button);
    }


    @Override
    public void setViewType(String type) {
        // removeComponent(getSelectedView().asVaadinComponent());
        final Component c = contentViews.get(type).asVaadinComponent();
        // addComponent(c, 1); // between tool bar and status bar
        // setExpandRatio(c, 1);
        keyboardEventPanel.setContent(c);

        if (SearchPresenterDefinition.VIEW_TYPE.equals(currentViewType) && !SearchPresenterDefinition.VIEW_TYPE.equals(type)) {
            setSearchQuery(null);
        }
        setViewTypeStyling(type);
        currentViewType = type;
    }

    private void fireViewTypeChangedEvent(String viewType) {
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

    private Button buildButton(final String viewType, final String icon) {
        NativeButton button = new NativeButton(null, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fireViewTypeChangedEvent(viewType);
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);

        button.setHtmlContentAllowed(true);
        button.setCaption("<span class=\"" + icon + "\"></span><span class=\"view-type-arrow view-type-arrow-" + viewType + " icon-arrow2_n\"></span>");
        return button;
    }

    private void setViewTypeStyling(final String viewType) {
        for (Map.Entry<String, Button> entry : contentViewsButton.entrySet()) {
            entry.getValue().removeStyleName("active");
            if (entry.getKey().equals(viewType)) {
                entry.getValue().addStyleName("active");
            }
        }
        // search is a list view
        if (viewType.equals(SearchPresenterDefinition.VIEW_TYPE) && contentViews.containsKey(ListPresenterDefinition.VIEW_TYPE)) {
            contentViewsButton.get(ListPresenterDefinition.VIEW_TYPE).addStyleName("active");
        }
    }

    private TextField buildSearchField() {
        final TextField field = new TextField();
        ShortcutProtector.extend(field, Arrays.asList(KeyCode.ENTER));
        final String inputPrompt = i18n.translate("toolbar.search.prompt");

        field.setInputPrompt(inputPrompt);
        field.setSizeUndefined();
        field.addStyleName("searchfield");

        // TextField has to be immediate to fire value changes when pressing Enter, avoiding ShortcutListener overkill.
        field.setImmediate(true);
        field.addValueChangeListener(searchFieldListener);

        // No blur handler.

        return field;
    }

    @Override
    public void setMultiselect(boolean multiselect) {
        for (String type : contentViews.keySet()) {
            contentViews.get(type).setMultiselect(multiselect);
        }
    }
}
