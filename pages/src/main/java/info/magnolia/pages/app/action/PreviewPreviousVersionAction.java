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
package info.magnolia.pages.app.action;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens a preview of the previous version of the selected page.
 */
public class PreviewPreviousVersionAction extends AbstractAction<PreviewPreviousVersionActionDefinition> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AbstractJcrNodeAdapter nodeItemToEdit;
    private final LocationController locationController;
    private final VersionManager versionManager;

    @Inject
    public PreviewPreviousVersionAction(PreviewPreviousVersionActionDefinition definition, AbstractJcrNodeAdapter nodeItemToEdit, LocationController locationController, VersionManager versionManager) {
        super(definition);
        this.nodeItemToEdit = nodeItemToEdit;
        this.locationController = locationController;
        this.versionManager = versionManager;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            final String path = nodeItemToEdit.getJcrItem().getPath();
            final String previousVersion = getPreviousVersion();
            DetailLocation location = new DetailLocation("pages", "detail", DetailView.ViewType.VIEW, path, previousVersion);
            locationController.goTo(location);

        } catch (RepositoryException e) {
            throw new ActionExecutionException("Could not execute PreviewPreviousVersionAction: ", e);
        }
    }

    /**
     * @return Last version if present or an empty string if no version are available.
     */
    private String getPreviousVersion() throws RepositoryException {
        String previousVersion = StringUtils.EMPTY;
        VersionIterator versionIterator = versionManager.getAllVersions(nodeItemToEdit.getJcrItem());
        // Check.
        if (versionIterator == null) {
            return previousVersion;
        }
        // Get last Version.
        while (versionIterator.hasNext()) {
            Version version = versionIterator.nextVersion();
            previousVersion = version.getName();
        }

        return previousVersion;
    }

}
