/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.contentapp.detail;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of an item subApp. Provides sensible implementation for
 * services shared by all item subApps. Implementers of this class represent a
 * tab for viewing and editing items typically opened from an {@link info.magnolia.ui.contentapp.browser.BrowserSubApp}. Subclasses can
 * augment the default behavior and perform additional tasks by overriding the
 * following methods:
 * <ul>
 * <li>{@link #onSubAppStart()}
 * <li>{@link #locationChanged(Location)}
 * </ul>
 * Currently lacking listeners for {@link info.magnolia.ui.api.event.ContentChangedEvent}. Currently
 * lacking handling of locationChanged. Related to MGNLUI-154
 *
 * @see DetailEditorPresenter
 * @see info.magnolia.ui.contentapp.ContentSubAppView
 * @see DetailLocation
 */
public class DetailSubApp extends BaseSubApp<ContentSubAppView> {

    private static final Logger log = LoggerFactory.getLogger(DetailSubApp.class);

    private final DetailEditorPresenter presenter;
    private final EventBus adminCentralEventBus;
    private final SimpleTranslator i18n;

    private Object itemId;

    private String caption;

    private ContentConnector contentConnector;

    @Inject
    protected DetailSubApp(final SubAppContext subAppContext, final ContentSubAppView view, @Named(AdmincentralEventBus.NAME) EventBus adminCentralEventBus,
            DetailEditorPresenter presenter, SimpleTranslator i18n, ContentConnector contentConnector) {
        super(subAppContext, view);

        this.adminCentralEventBus = adminCentralEventBus;
        this.presenter = presenter;
        this.i18n = i18n;
        this.contentConnector = contentConnector;
        bindHandlers();
    }

    /**
     * Performs some routine tasks needed by all item subApps before the view is displayed.
     * The tasks are:
     * <ul>
     * <li>setting the current location
     * <li>setting the presenter's view
     * <li>calling {@link #onSubAppStart()} a hook-up method subclasses can override to perform additional work.
     * </ul>
     */
    @Override
    public ContentSubAppView start(final Location location) {
        DetailLocation detailLocation = DetailLocation.wrap(location);
        super.start(detailLocation);
        // set caption
        setCaption(detailLocation);
        this.itemId = contentConnector.getItemIdByUrlFragment(detailLocation.getNodePath());

        View view;
        if (detailLocation.hasVersion()) {
            view = presenter.start(detailLocation.getNodePath(), detailLocation.getViewType(), contentConnector, detailLocation.getVersion());
        } else {
            view = presenter.start(detailLocation.getNodePath(), detailLocation.getViewType(), contentConnector);
        }
        getView().setContentView(view);
        return getView();
    }

    /**
     * Wraps the current DefaultLocation in a ItemLocation. Providing getter and setters for used parameters.
     */
    @Override
    public DetailLocation getCurrentLocation() {
        return DetailLocation.wrap(super.getCurrentLocation());
    }

    @Override
    public boolean supportsLocation(Location location) {
        DetailLocation itemLocation = DetailLocation.wrap(location);
        String currentPath = getCurrentLocation().getNodePath();
        return currentPath.equals(itemLocation.getNodePath());
    }

    /**
     * On location change, reload the view and tab caption.
     */
    @Override
    public void locationChanged(Location location) {
        DetailLocation detailLocation = DetailLocation.wrap(location);
        if (!detailLocation.equals(getCurrentLocation())) {
            setCaption(detailLocation);
            View view = presenter.update(detailLocation);
            getView().setContentView(view);
        }
    }

    @Override
    public String getCaption() {
        return caption;
    }

    private void bindHandlers() {
        adminCentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                // See if workspaces match
                if (contentConnector.canHandleItem(event.getItemId())) {
                    // New item
                    if (itemId == null) {
                        // Check if parent is still existing, close supApp if it doesn't
                        String currentNodePath = getCurrentLocation().getNodePath();

                        // resolve parent, removing trailing slash except for root
                        int splitIndex = currentNodePath.lastIndexOf("/");
                        if (splitIndex == 0) {
                            splitIndex = 1;
                        }
                        String parentNodePath = currentNodePath.substring(0, splitIndex);
                        Object parentItemId = contentConnector.getItemIdByUrlFragment(parentNodePath);
                        if (!contentConnector.canHandleItem(parentItemId)) {
                            getSubAppContext().close();
                        }
                        // Editing existing item
                    } else {
                        // Item (or parent) was deleted: close subApp
                        if (!contentConnector.canHandleItem(itemId)) {
                            getSubAppContext().close();
                        }
                        // Item still exists: update location if necessary
                        else {
                            String currentNodePath = getCurrentLocation().getNodePath();
                            String itemPath = contentConnector.getItemUrlFragment(itemId);
                            if (!currentNodePath.equals(itemPath)) {
                                DetailLocation location = DetailLocation.wrap(getSubAppContext().getLocation());
                                location.updateNodePath(itemPath);
                                // Update location
                                getSubAppContext().setLocation(location);
                                // Update Caption
                                setCaption(location);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Set the Tab caption.
     * If a version is part of the {@link DetailLocation}, add this version information to the Tab caption.
     */
    protected void setCaption(DetailLocation location) {
        String caption = getBaseCaption(location);
        // Set version information
        if (StringUtils.isNotBlank(location.getVersion())) {
            caption = i18n.translate("subapp.versioned_page", caption, location.getVersion() );
        }
        this.caption = caption;
    }

    /**
     * Create the base caption string.
     * Default is the item path.
     */
    protected String getBaseCaption(DetailLocation location) {
        return location.getNodePath();
    }

}
