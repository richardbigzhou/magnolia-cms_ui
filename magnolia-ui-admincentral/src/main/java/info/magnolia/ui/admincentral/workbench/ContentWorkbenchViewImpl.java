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
import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.BaseTheme;


/**
 * Implementation of {@link ContentWorkbenchView}.
 */
public class ContentWorkbenchViewImpl extends CustomComponent implements ContentWorkbenchView {

    private final HorizontalLayout root = new HorizontalLayout();

    private final CssLayout contentViewContainer = new CssLayout();

    private final Button treeButton;

    private final Button listButton;

    private final Button thumbsButton;

    private final TextField searchbox;

    private final Embedded viewTypeArrow;

    private Map<ViewType, ContentView> contentViews = new EnumMap<ViewType, ContentView>(ViewType.class);

    private ActionbarView actionbar;

    private ViewType currentViewType = ViewType.TREE;

    /** for going back from search view if search expression is empty. */
    private ViewType previousViewType = currentViewType;

    private ContentWorkbenchView.Listener contentWorkbenchViewListener;

    public ContentWorkbenchViewImpl() {
        super();
        setCompositionRoot(root);
        setSizeFull();

        root.setSizeFull();
        root.setStyleName("workbench");
        root.addComponent(contentViewContainer);
        root.setExpandRatio(contentViewContainer, 1);
        root.setSpacing(true);
        root.setMargin(false);

        CssLayout viewModes = new CssLayout();
        viewModes.setStyleName("view-modes");
        viewModes.setMargin(false);

        treeButton = buildButton(ViewType.TREE, "icon-view-tree", true);
        listButton = buildButton(ViewType.LIST, "icon-view-list", false);
        thumbsButton = buildButton(ViewType.THUMBNAIL, "icon-view-thumbnails", false);
        viewTypeArrow = buildViewTypeArrow();
        searchbox = buildBasicSearchbox();

        viewModes.addComponent(treeButton);
        viewModes.addComponent(listButton);
        viewModes.addComponent(thumbsButton);

        contentViewContainer.setSizeFull();
        contentViewContainer.addComponent(viewModes);
        contentViewContainer.addComponent(viewTypeArrow);
        contentViewContainer.addComponent(searchbox);
    }

    private TextField buildBasicSearchbox() {
        final TextField searchbox = new TextField();
        final String inputPrompt = MessagesUtil.getWithDefault("toolbar.search.prompt", "Search");

        searchbox.setInputPrompt(inputPrompt);
        searchbox.setSizeUndefined();
        searchbox.addStyleName("searchbox");

        searchbox.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ENTER, null) {

            @Override
            public void handleAction(Object sender, Object target) {
                contentWorkbenchViewListener.onSearch(searchbox.getValue().toString());
            }
        });

        searchbox.addListener(new FieldEvents.BlurListener() {

            @Override
            public void blur(BlurEvent event) {
                contentWorkbenchViewListener.onSearch(searchbox.getValue().toString());
                searchbox.setInputPrompt(inputPrompt);
            }
        });

        searchbox.addListener(new FieldEvents.FocusListener() {

            @Override
            public void focus(FocusEvent event) {
                if (currentViewType != ViewType.SEARCH) {
                    setViewType(ViewType.SEARCH);
                }
            }
        });

        return searchbox;
    }

    private Embedded buildViewTypeArrow() {
        ThemeResource img = new ThemeResource("img/arrow-up-white.png");
        Embedded arrow = new Embedded(null, img);
        arrow.setType(Embedded.TYPE_IMAGE);
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

        if (type != ViewType.SEARCH) {
            previousViewType = currentViewType;
        }

        contentViewContainer.removeComponent(contentViews.get(currentViewType).asVaadinComponent());
        final Component c = contentViews.get(type).asVaadinComponent();

        c.setSizeFull();
        contentViewContainer.addComponent(c);

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
        actionbar.asVaadinComponent().setWidth(Sizeable.SIZE_UNDEFINED, 0);
        if (this.actionbar == null) {
            root.addComponent(actionbar.asVaadinComponent());
        } else {
            root.replaceComponent(this.actionbar.asVaadinComponent(), actionbar.asVaadinComponent());
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
            case TREE :
                treeButton.addStyleName("active");
                viewTypeArrow.addStyleName("tree");
                break;
            case LIST :
                listButton.addStyleName("active");
                viewTypeArrow.addStyleName("list");
                break;
            case THUMBNAIL :
                thumbsButton.addStyleName("active");
                viewTypeArrow.addStyleName("thumbs");
                break;
            case SEARCH :
                viewTypeArrow.addStyleName("search");
                break;
            default :
        }
    }

    @Override
    public void resynch(final String path, final ViewType viewType, final String query) {
        setViewType(viewType);
        if (viewType == ViewType.SEARCH) {
            if (StringUtils.isBlank(query)) {
                setViewType(previousViewType);
            } else {
                searchbox.setValue(query);
                searchbox.focus();
                contentWorkbenchViewListener.onSearch(query);
            }
        }
        selectPath(path);
    }

}
