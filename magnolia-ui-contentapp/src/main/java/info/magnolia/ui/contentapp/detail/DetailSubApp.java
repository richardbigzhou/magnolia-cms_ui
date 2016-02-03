/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
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

    private String itemId;
    private String caption;

    @Inject
    protected DetailSubApp(final SubAppContext subAppContext, final ContentSubAppView view, @Named(AdmincentralEventBus.NAME) EventBus adminCentralEventBus,
            DetailEditorPresenter presenter, SimpleTranslator i18n) {
        super(subAppContext, view);

        this.adminCentralEventBus = adminCentralEventBus;
        this.presenter = presenter;
        this.i18n = i18n;

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
        try {
            this.itemId = JcrItemUtil.getItemId(getWorkspace(), detailLocation.getNodePath());
        } catch (RepositoryException e) {
            log.warn("Could not retrieve item at path {} in workspace {}", detailLocation.getNodePath(), getWorkspace());
        }

        View view;
        if (detailLocation.hasVersion()) {
            view = presenter.start(detailLocation.getNodePath(), detailLocation.getViewType(), detailLocation.getVersion());
        } else {
            view = presenter.start(detailLocation.getNodePath(), detailLocation.getViewType());
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
                if (event.getWorkspace().equals(getWorkspace())) {
                    // New item
                    if (itemId == null) {
                        try {
                            // Check if parent is still existing, close supApp if it doesn't
                            String currentNodePath = getCurrentLocation().getNodePath();

                            // resolve parent, removing trailing slash except for root
                            int splitIndex = currentNodePath.lastIndexOf("/");
                            if (splitIndex == 0) {
                                splitIndex = 1;
                            }
                            String parentNodePath = currentNodePath.substring(0, splitIndex);

                            if (!MgnlContext.getJCRSession(getWorkspace()).nodeExists(parentNodePath)) {
                                getSubAppContext().close();
                            }
                        } catch (RepositoryException e) {
                            log.warn("Could not determine if parent node still exists", e);
                        }
                        // Editing existing item
                    } else {
                        try {
                            Item item = JcrItemUtil.getJcrItem(getWorkspace(), itemId);

                            // Item (or parent) was deleted: close subApp
                            if (item == null) {
                                getSubAppContext().close();
                            }
                            // Item still exists: update location if necessary
                            else {
                                String currentNodePath = getCurrentLocation().getNodePath();

                                if (!item.getPath().equals(currentNodePath)) {
                                    DetailLocation location = DetailLocation.wrap(getSubAppContext().getLocation());
                                    location.updateNodePath(item.getPath());
                                    // Update location
                                    getSubAppContext().setLocation(location);
                                    // Update Caption
                                    setCaption(location);
                                }
                            }
                        } catch (RepositoryException e) {
                            log.warn("Could not determine if node still exists", e);
                        }
                    }
                }
            }
        });

    }

    protected String getWorkspace() {
        DetailSubAppDescriptor subAppDescriptor = (DetailSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        return subAppDescriptor.getEditor().getWorkspace();
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
