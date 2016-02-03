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
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restore the previous version of a page and edit it.
 */
public class RestorePreviousVersionAction extends AbstractAction<RestorePreviousVersionActionDefinition> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AbstractJcrNodeAdapter nodeItemToEdit;
    private final LocationController locationController;
    private final VersionManager versionManager;
    private final SubAppContext subAppContext;
    private final SimpleTranslator i18n;

    @Inject
    public RestorePreviousVersionAction(RestorePreviousVersionActionDefinition definition, AbstractJcrNodeAdapter nodeItemToEdit, LocationController locationController,
            VersionManager versionManager, SubAppContext subAppContext, SimpleTranslator i18n) {
        super(definition);
        this.nodeItemToEdit = nodeItemToEdit;
        this.locationController = locationController;
        this.versionManager = versionManager;
        this.subAppContext = subAppContext;
        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            if (StringUtils.isNotBlank(getDefinition().getNodeType()) && !getDefinition().getNodeType().equals(nodeItemToEdit.getJcrItem().getPrimaryNodeType().getName())) {
                log.warn("RestorePreviousVersionAction requested for a node type definition {}. Current node type is {}. No action will be performed.",
                        getDefinition().getNodeType(), nodeItemToEdit.getJcrItem().
                        getPrimaryNodeType().getName());
                return;
            }
            final String path = nodeItemToEdit.getJcrItem().getPath();

            // Get last version.
            Version version = getPreviousVersion();
            // Check the version.
            if (version == null) {
                //
                subAppContext.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("pages.restorePreviousVersionAction.noVersion.actionCanceled.message"));
                return;
            }
            // Restore previous version
            versionManager.restore(nodeItemToEdit.getJcrItem(), version, true);
            DetailLocation location = new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, path, "");
            locationController.goTo(location);

        } catch (RepositoryException e) {
            subAppContext.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("pages.restorePreviousVersionAction.repositoryException.actionCanceled.message"));
            throw new ActionExecutionException("Could not execute RestorePreviousVersionAction: ", e);
        }
    }

    /**
     * @return Previous version or null if not founded.
     */
    private Version getPreviousVersion() throws RepositoryException {
        Version previousVersion = null;
        VersionIterator versionIterator = versionManager.getAllVersions(nodeItemToEdit.getJcrItem());
        if (versionIterator == null) {
            return previousVersion;
        }

        while (versionIterator.hasNext()) {
            previousVersion = versionIterator.nextVersion();
        }

        return previousVersion;
    }

}
