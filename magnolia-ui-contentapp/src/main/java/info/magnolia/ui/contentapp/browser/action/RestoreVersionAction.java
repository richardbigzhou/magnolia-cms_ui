/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser.action;

import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.VersionUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI action which allow to restore node to the state defined by the selected version.
 */
public class RestoreVersionAction extends ShowVersionsAction<RestoreVersionActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(RestoreVersionAction.class);

    private final VersionManager versionManager;

    private final EventBus eventBus;

    private final VersionConfig versionConfig;

    /**
     * @deprecated since 5.3.5 - use {@link RestoreVersionAction(RestoreVersionActionDefinition, AppContext, LocationController, UiContext, FormDialogPresenter, AbstractJcrNodeAdapter, SimpleTranslator, VersionManager, EventBus, VersionConfig)} instead.
     */
    @Deprecated
    public RestoreVersionAction(RestoreVersionActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, VersionManager versionManager, final @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        this(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n, versionManager, eventBus, Components.getComponent(VersionConfig.class));
    }

    @Inject
    public RestoreVersionAction(RestoreVersionActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, VersionManager versionManager, final @Named(AdmincentralEventBus.NAME) EventBus eventBus, VersionConfig versionConfig) {
        super(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n);
        this.versionManager = versionManager;
        this.eventBus = eventBus;
        this.versionConfig = versionConfig;
        this.dialogID = "ui-contentapp:code:RestoreVersionAction.selectVersion";
    }

    @Override
    protected EditorCallback getEditorCallback() {
        return new EditorCallback() {
            @Override
            public void onSuccess(String actionName) {
                try {
                    Node node = getNode();
                    String versionName = getVersionName();
                    VersionHistory versionHistory = versionManager.getVersionHistory(node);

                    if (getDefinition().isCreateVersionBeforeRestore()) {
                        long versionsSize = versionHistory.getAllVersions().getSize() - 2; // Do not consider the root version

                        if (!VersionUtil.hasPreviousVersion(node, versionName) && versionsSize >= versionConfig.getMaxVersionAllowed()) {
                            uiContext.openConfirmation(
                                    MessageStyleTypeEnum.WARNING,
                                    i18n.translate("ui-contentapp.actions.restoreVersion.confirmation.title"),
                                    i18n.translate("ui-contentapp.actions.restoreVersion.confirmation.body"),
                                    i18n.translate("ui-contentapp.actions.restoreVersion.confirmation.confirmButton"),
                                    i18n.translate("ui-contentapp.actions.restoreVersion.confirmation.cancelButton"),
                                    false,
                                    getConfirmationCallback());
                        } else {
                            restoreVersion(node, versionName, true);
                        }
                    } else {
                        restoreVersion(node, versionName, false);
                    }
                } catch (RepositoryException e) {
                    uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("ui-contentapp.actions.restoreVersion.notification.error", e.getMessage()));
                    log.error(i18n.translate("ui-contentapp.actions.restoreVersion.notification.error", e.getMessage()), e);
                }
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        };
    }

    /**
     * Confirmation callback when version store is full and user has to decide whether to restore the last version
     * without creating another version of the page before restoring.
     */
    protected ConfirmationCallback getConfirmationCallback() {
        return new ConfirmationCallback() {
            @Override
            public void onSuccess() {
                try {
                    restoreVersion(getNode(), getVersionName(), false);
                } catch (RepositoryException e) {
                    log.error(i18n.translate("ui-contentapp.actions.restoreVersion.notification.error", e.getMessage()), e);
                }
            }

            @Override
            public void onCancel() {
                // Just close the confirmation but do not close the dialog
            }
        };
    }

    /**
     * Creates a version with an extra comment before restoring.
     */
    protected Version createVersionBeforeRestore(Node node) throws RepositoryException {
        NodeTypes.Versionable.set(node, "ui-contentapp.actions.restoreVersion.comment.restore");
        node.getSession().save();
        Version version = versionManager.addVersion(node);
        NodeTypes.Versionable.set(node, null);
        node.getSession().save();
        return version;
    }

    /**
     * Restores a version by its version name.
     *
     * By default {@link RestoreVersionActionDefinition#createVersionBeforeRestore} is true thus a version is created
     * before restore. When {@link VersionConfig#maxVersions} is reached such a version cannot be created when restoring
     * the oldest version (without deleting that same version). In this case {@link #getConfirmationCallback()} is shown
     * and once confirmed the version gets restored without "backup" of the current content.
     *
     * @see <a href="http://jira.magnolia-cms.com/browse/MGNLUI-3220">MGNLUI-3220</a>
     */
    private void restoreVersion(Node node, String versionName, boolean createVersionBeforeRestore) throws RepositoryException {
        final Version versionToRestore = versionManager.getVersion(node, versionName);

        // Create another version before restore
        if (createVersionBeforeRestore) {
            createVersionBeforeRestore(node);
        }

        // Restore version
        versionManager.restore(node, versionToRestore, true);

        eventBus.fireEvent(new ContentChangedEvent(nodeAdapter.getItemId()));
        uiContext.openNotification(MessageStyleTypeEnum.INFO, true, i18n.translate("ui-contentapp.actions.restoreVersion.notification.success"));

        formDialogPresenter.closeDialog();

        log.debug("Restored version [{}]", versionName);
    }

}
