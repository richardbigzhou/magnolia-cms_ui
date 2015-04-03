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
package info.magnolia.ui.contentapp.detail.action;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restore the previous version of an item.
 * 
 * @deprecated since 5.3.4, please use {@link info.magnolia.ui.contentapp.browser.action.RestoreItemPreviousVersionAction} instead.
 */
@Deprecated
public class RestorePreviousVersionAction extends AbstractAction<RestorePreviousVersionActionDefinition> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AbstractJcrNodeAdapter nodeItemToEdit;
    private final VersionManager versionManager;
    private final SubAppContext subAppContext;
    private final EventBus eventBus;
    private final SimpleTranslator i18n;

    @Inject
    public RestorePreviousVersionAction(RestorePreviousVersionActionDefinition definition, AbstractJcrNodeAdapter nodeItemToEdit,
            VersionManager versionManager, SubAppContext subAppContext, final @Named(AdmincentralEventBus.NAME) EventBus eventBus, SimpleTranslator i18n) {
        super(definition);
        this.nodeItemToEdit = nodeItemToEdit;
        this.versionManager = versionManager;
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            // Get last version.
            Version version = getPreviousVersion();
            // Check the version.
            if (version == null) {
                subAppContext.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("ui-contentapp.actions.restorePreviousVersion.notification.error"));
                return;
            }
            // Restore previous version
            versionManager.restore(nodeItemToEdit.getJcrItem(), version, true);
            eventBus.fireEvent(new ContentChangedEvent(nodeItemToEdit.getItemId()));
            subAppContext.openNotification(MessageStyleTypeEnum.INFO, true, i18n.translate("ui-contentapp.actions.restorePreviousVersion.notification.success"));
        } catch (RepositoryException e) {
            subAppContext.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("ui-contentapp.actions.restorePreviousVersion.notification.error"));
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
