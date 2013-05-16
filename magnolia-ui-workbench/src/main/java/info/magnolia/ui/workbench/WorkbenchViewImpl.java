/**
 * This file Copyright (c) 2013 Magnolia International
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
import info.magnolia.ui.statusbar.StatusBarView;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Implementation of the workbench view.
 */
public class WorkbenchViewImpl extends VerticalLayout implements WorkbenchView {

    private final CssLayout toolBar = new CssLayout();

    private final CssLayout viewModes = new CssLayout();

    private TextField searchBox;

    private StatusBarView statusBar;

    private Map<ViewType, ContentView> contentViews = new EnumMap<ViewType, ContentView>(ViewType.class);

    private Map<ViewType, Button> contentViewsButton = new EnumMap<ViewType, Button>(ViewType.class);

    private ViewType currentViewType = ViewType.TREE;

    /**
     * for going back from search view if search expression is empty.
     */
    private ViewType previousViewType = currentViewType;

    private final Property.ValueChangeListener searchBoxListener = new Property.ValueChangeListener() {

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            listener.onSearch(searchBox.getValue().toString());
        }
    };

    private WorkbenchView.Listener listener;

    public WorkbenchViewImpl(){

        setSizeFull();
        setMargin(new MarginInfo(true, false, false, true));
        addStyleName("workbench");

        viewModes.setStyleName("view-modes");

        searchBox = buildBasicSearchBox();
        searchBox.setVisible(false);

        toolBar.addStyleName("toolbar");
        toolBar.setWidth(100, Unit.PERCENTAGE);
        toolBar.addComponent(viewModes);
        toolBar.addComponent(searchBox);

        addComponent(toolBar);
        setExpandRatio(toolBar, 0);
    }

    @Override
    public void setSearchQuery(String query) {
        if (searchBox == null) {
            return;
        }
        // turn off value change listener, so that presenter does not think there was user input and searches again
        searchBox.removeValueChangeListener(searchBoxListener);
        if (StringUtils.isNotBlank(query)) {
            searchBox.setValue(query);
            searchBox.focus();
        } else {
            searchBox.setValue("");
        }
        searchBox.addValueChangeListener(searchBoxListener);
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

    private TextField buildBasicSearchBox() {
        final TextField searchBox = new TextField();
        final String inputPrompt = MessagesUtil.getWithDefault("toolbar.search.prompt", "Search");

        searchBox.setInputPrompt(inputPrompt);
        searchBox.setSizeUndefined();
        searchBox.addStyleName("searchbox");

        // TextField has to be immediate to fire value changes when pressing Enter, avoiding ShortcutListener overkill.
        searchBox.setImmediate(true);
        searchBox.addListener(searchBoxListener);

        searchBox.addBlurListener(new FieldEvents.BlurListener() {
            @Override
            public void blur(FieldEvents.BlurEvent event) {
                // return to previous view type when leaving empty field
                if (StringUtils.isBlank(searchBox.getValue().toString())) {
                    fireViewTypeChangedEvent(previousViewType);
                }
            }
        });

        searchBox.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                // put the cursor at the end of the field
                TextField tf = (TextField) event.getSource();
                tf.setCursorPosition(tf.getValue().length());
            }
        });
        return searchBox;
    }
}
