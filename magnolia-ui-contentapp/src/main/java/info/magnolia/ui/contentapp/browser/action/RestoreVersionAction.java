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

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

/**
 * UI action which allow to restore node to the state defined by the selected version.
 */
public class RestoreVersionAction extends ShowVersionsAction<RestoreVersionActionDefinition>{

    private static final Logger log = LoggerFactory.getLogger(RestoreVersionAction.class);

    private final VersionManager versionManager;

    private final EventBus eventBus;

    @Inject
    public RestoreVersionAction(RestoreVersionActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, VersionManager versionManager, final @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n);
        this.versionManager = versionManager;
        this.eventBus = eventBus;
        this.dialogID = "ui-contentapp:code:RestoreVersionAction.selectVersion";
    }

    @Override
    protected EditorCallback getEditorCallback() {
        return new EditorCallback() {
            @Override
            public void onSuccess(String actionName) {
                try {
                    Node node = getNode();

                    if(getDefinition().isCreateVersionBeforeRestore()){
                        createVersionBeforeRestore(node);
                    }

                    String versionName = (String) getItem().getItemProperty("versionName").getValue();
                    versionManager.restore(node, versionManager.getVersion(node, versionName), true);
                    eventBus.fireEvent(new ContentChangedEvent(nodeAdapter.getWorkspace(), nodeAdapter.getItemId()));
                    uiContext.openNotification(MessageStyleTypeEnum.INFO, true, i18n.translate("ui-contentapp.actions.restoreVersion.notification.success"));
                } catch (RepositoryException e) {
                    uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("ui-contentapp.actions.restoreVersion.notification.error", e.getMessage()));
                    log.error(i18n.translate("ui-contentapp.actions.restoreVersion.notification.error", e.getMessage()));
                }

                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        };
    }

    protected Version createVersionBeforeRestore(Node node) throws RepositoryException {
        NodeTypes.Versionable.set(node, i18n.translate("ui-contentapp.actions.restoreVersion.comment.restore"));
        node.getSession().save();
        Version version = versionManager.addVersion(node);
        NodeTypes.Versionable.set(node, null);
        node.getSession().save();
        return version;
    }

}
