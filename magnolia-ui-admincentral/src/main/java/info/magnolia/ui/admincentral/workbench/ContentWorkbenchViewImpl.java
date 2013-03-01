/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.workbench;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.workbench.ContentView;
import info.magnolia.ui.workbench.ContentView.ViewType;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Implementation of {@link ContentWorkbenchView}.
 */
public class ContentWorkbenchViewImpl extends HorizontalLayout implements ContentWorkbenchView {

    private final CssLayout contentViewContainer = new CssLayout();

    private final Button treeButton;

    private final Button listButton;

    private final Button thumbsButton;

    private final TextField searchbox;

    private final Property.ValueChangeListener searchboxListener = new Property.ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
            contentWorkbenchViewListener.onSearch(searchbox.getValue().toString());
        }
    };

    private final Component viewTypeArrow;

    private Map<ViewType, ContentView> contentViews = new EnumMap<ViewType, ContentView>(ViewType.class);

    private ActionbarView actionbar;

    private ViewType currentViewType = ViewType.TREE;

    /**
     * for going back from search view if search expression is empty.
     */
    private ViewType previousViewType = currentViewType;

    private ContentWorkbenchView.Listener contentWorkbenchViewListener;

    public ContentWorkbenchViewImpl() {
        super();
        setSizeFull();
        setStyleName("workbench");
        addComponent(contentViewContainer);
        setExpandRatio(contentViewContainer, 1);
        setSpacing(true);
        setMargin(true);

        CssLayout viewModes = new CssLayout();
        viewModes.setStyleName("view-modes");

        treeButton = buildButton(ViewType.TREE, "icon-view-tree", true);
        listButton = buildButton(ViewType.LIST, "icon-view-list", false);
        thumbsButton = buildButton(ViewType.THUMBNAIL, "icon-view-thumbnails", false);
        viewTypeArrow = buildViewTypeArrow();
        searchbox = buildBasicSearchbox();

        viewModes.addComponent(treeButton);
        viewModes.addComponent(listButton);
        viewModes.addComponent(thumbsButton);

        contentViewContainer.addStyleName("v-workbench-content");
        contentViewContainer.setSizeFull();
        contentViewContainer.addComponent(searchbox);
        contentViewContainer.addComponent(viewModes);
        contentViewContainer.addComponent(viewTypeArrow);
    }

    private TextField buildBasicSearchbox() {
        final TextField searchbox = new TextField();
        final String inputPrompt = MessagesUtil.getWithDefault("toolbar.search.prompt", "Search");

        searchbox.setInputPrompt(inputPrompt);
        searchbox.setSizeUndefined();
        searchbox.addStyleName("searchbox");

        // Textfield has to be immediate to fire value changes when pressing Enter, avoiding ShortcutListener overkill.
        searchbox.setImmediate(true);
        searchbox.addListener(searchboxListener);

        searchbox.addBlurListener(new BlurListener() {
            @Override
            public void blur(BlurEvent event) {
                // return to previous view type when leaving empty field
                if (StringUtils.isBlank(searchbox.getValue().toString())) {
                    setViewType(previousViewType);
                }
            }
        });

        searchbox.addFocusListener(new FocusListener() {
            @Override
            public void focus(FocusEvent event) {
                // put the cursor at the end of the field
                TextField tf = (TextField) event.getSource();
                tf.setCursorPosition(tf.toString().length());
            }
        });
        return searchbox;
    }

    private Component buildViewTypeArrow() {
        Label arrow = new Label();
        arrow.setSizeUndefined();
        arrow.addStyleName("view-type-arrow");
        return arrow;
    }

    private Button buildButton(final ViewType viewType, final String icon, final boolean active) {
        NativeButton button = new NativeButton(null, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                setViewType(viewType);
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);

        button.setHtmlContentAllowed(true);
        button.setCaption("<span class=\"" + icon + "\"></span>");
        if (active) {
            button.addStyleName("active");
        }
        return button;
    }

    public ContentWorkbenchView.Listener getListener() {
        return contentWorkbenchViewListener;
    }

    @Override
    public void setListener(final ContentWorkbenchView.Listener listener) {
        this.contentWorkbenchViewListener = listener;
    }

    @Override
    public void setViewType(final ViewType type) {
        contentViewContainer.removeComponent(contentViews.get(currentViewType).asVaadinComponent());
        final Component c = contentViews.get(type).asVaadinComponent();
        c.setSizeFull();
        contentViewContainer.addComponent(c);
        c.setSizeUndefined();

        if (type != ViewType.SEARCH) {
            previousViewType = type;
            setSearchQuery(null);
        }
        this.currentViewType = type;

        setViewTypeStyling(currentViewType);
        refresh();

        this.contentWorkbenchViewListener.onViewTypeChanged(currentViewType);
    }

    @Override
    public void refresh() {
        contentViews.get(currentViewType).refresh();
    }

    public void setContentViews(Map<ViewType, ContentView> contentViews) {
        this.contentViews = contentViews;
    }

    @Override
    public void addContentView(final ViewType type, final ContentView view) {
        contentViews.put(type, view);
    }

    @Override
    public void setActionbarView(final ActionbarView actionbar) {
        actionbar.asVaadinComponent().setWidth(null);
        if (this.actionbar == null) {
            addComponent(actionbar.asVaadinComponent());
        } else {
            replaceComponent(this.actionbar.asVaadinComponent(), actionbar.asVaadinComponent());
        }
        this.actionbar = actionbar;
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void selectPath(String path) {
        for (ContentView contentView : contentViews.values()) {
            contentView.select(path);
        }
    }

    @Override
    public ContentView getSelectedView() {
        return contentViews.get(currentViewType);
    }

    private void setViewTypeStyling(final ViewType viewType) {
        treeButton.removeStyleName("active");
        listButton.removeStyleName("active");
        thumbsButton.removeStyleName("active");

        viewTypeArrow.removeStyleName("tree");
        viewTypeArrow.removeStyleName("list");
        viewTypeArrow.removeStyleName("thumbs");
        viewTypeArrow.removeStyleName("search");

        switch (viewType) {
        case TREE:
            treeButton.addStyleName("active");
            viewTypeArrow.addStyleName("tree");
            break;
        case LIST:
            listButton.addStyleName("active");
            viewTypeArrow.addStyleName("list");
            break;
        case THUMBNAIL:
            thumbsButton.addStyleName("active");
            viewTypeArrow.addStyleName("thumbs");
            break;
        case SEARCH:
            viewTypeArrow.addStyleName("search");
            break;
        default:
        }
    }

    @Override
    public void setSearchQuery(final String query) {
        // turn off value change listener, so that presenter does not think there was user input and searches again
        searchbox.removeListener(searchboxListener);
        if (StringUtils.isNotBlank(query)) {
            searchbox.setValue(query);
            searchbox.focus();
        } else {
            searchbox.setValue("");
        }
        searchbox.addListener(searchboxListener);
    }

}
