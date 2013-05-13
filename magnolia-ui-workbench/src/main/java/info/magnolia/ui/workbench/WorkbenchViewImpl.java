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
 * TODO: Add JavaDoc for WorkbenchViewImpl.
 */
public class WorkbenchViewImpl extends VerticalLayout implements WorkbenchView {

    private final CssLayout toolBar = new CssLayout();

    private final CssLayout viewModes = new CssLayout();

    private TextField searchBox;

    private StatusBarView statusBar;

    private Map<ContentView.ViewType, ContentView> contentViews = new EnumMap<ContentView.ViewType, ContentView>(ContentView.ViewType.class);

    private Map<ContentView.ViewType, Button> contentViewsButton = new EnumMap<ContentView.ViewType, Button>(ContentView.ViewType.class);

    private ContentView.ViewType currentViewType = ContentView.ViewType.TREE;

    /**
     * for going back from search view if search expression is empty.
     */
    private ContentView.ViewType previousViewType = currentViewType;

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
        searchBox.addValueChangeListener(searchBoxListener);
        if (StringUtils.isNotBlank(query)) {
            searchBox.setValue(query);
            searchBox.focus();
        } else {
            searchBox.setValue("");
        }
        searchBox.addValueChangeListener(searchBoxListener);
    }

    @Override
    public void refresh() {
        getSelectedView().refresh();
    }

    @Override
    public void addContentView(ContentView.ViewType viewType, ContentView view, ContentViewDefinition contentViewDefintion) {
        contentViews.put(viewType, view);

        if(viewType.equals(ContentView.ViewType.SEARCH)) {
            // Do not add a Button for Search
            return;
        }
        if (viewType.equals(ContentView.ViewType.LIST)) {
            searchBox.setVisible(true);
        }

        // Set Button
        Button button = buildButton(viewType, contentViewDefintion.getIcon(), contentViewDefintion.isActive());
        contentViewsButton.put(viewType, button);
        viewModes.addComponent(button);
        // Set Active
        if (contentViewDefintion.isActive()) {
            currentViewType = previousViewType = viewType;
        }
    }

    @Override
    public void setViewType(ContentView.ViewType type) {
        removeComponent(getSelectedView().asVaadinComponent());
        final Component c = contentViews.get(type).asVaadinComponent();
        addComponent(c, 1); // between tool bar and status bar
        setExpandRatio(c, 1);

        if (type != ContentView.ViewType.SEARCH) {
            previousViewType = type;
            setSearchQuery(null);
        }
        this.currentViewType = type;

        setViewTypeStyling(currentViewType);
        refresh();

        this.listener.onViewTypeChanged(currentViewType);
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
    public void selectPath(String path) {
        getSelectedView().select(path);
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

    private Button buildButton(final ContentView.ViewType viewType, final String icon, final boolean active) {
        NativeButton button = new NativeButton(null, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                setViewType(viewType);
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

    private void setViewTypeStyling(final ContentView.ViewType viewType) {
        for (Map.Entry<ContentView.ViewType, Button> entry : contentViewsButton.entrySet()) {
            entry.getValue().removeStyleName("active");
            if (entry.getKey().equals(viewType)) {
                // Set Active
                entry.getValue().addStyleName("active");
            }
        }
        // Handle Search (Not part of the Button List)
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
                    setViewType(previousViewType);
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
