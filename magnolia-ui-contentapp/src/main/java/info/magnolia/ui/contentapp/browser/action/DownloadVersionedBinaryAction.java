/**
 * This file Copyright (c) 2014 Magnolia International
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
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.framework.action.DownloadBinaryAction;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download versioned binary content.
 */
public class DownloadVersionedBinaryAction extends ShowVersionsAction<DownloadVersionedBinaryActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(DownloadVersionedBinaryAction.class);

    private final DownloadVersionedBinaryActionDefinition definition;
    private final JcrItemAdapter nodeAdapter;
    private final VersionManager versionManager;

    @Inject
    public DownloadVersionedBinaryAction(DownloadVersionedBinaryActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, VersionManager versionManager) {
        super(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n);

        this.nodeAdapter = nodeAdapter;
        this.definition = definition;
        this.versionManager = versionManager;

        this.dialogID = "ui-contentapp:code:DownloadVersionedBinaryAction.downloadVersion";
    }

    @Override
    protected EditorCallback getEditorCallback() {
        return new EditorCallback() {
            @Override
            public void onSuccess(String actionName) {
                if (nodeAdapter instanceof JcrNodeAdapter) {
                    final String versionName = getVersionName();
                    final Node node = (Node) nodeAdapter.getJcrItem();
                    final Version version;

                    try {
                        version = versionManager.getVersion(node, versionName);

                        new DownloadBinaryAction(definition, new JcrNodeAdapter(version)).execute();
                    } catch (RepositoryException e) {
                        log.error("Error getting binary node [{}] from version [{}] of node [{}]", definition.getBinaryNodeName(), versionName, node, e);
                    } catch (ActionExecutionException e) {
                        log.error("Error executing download action for versioned node [{}]", node, e);
                    }
                }

                // Close the dialog
                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        };
    }

}
