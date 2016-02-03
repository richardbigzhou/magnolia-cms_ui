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
package info.magnolia.ui.contentapp.browser.action;

import info.magnolia.cms.core.version.VersionInfo;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.framework.action.AbstractVersionAction;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Opens a dialog with list of versions.
 *
 * @see ShowVersionsActionDefinition
 * @param <D> {@link ShowVersionsActionDefinition}.
 */
public class ShowVersionsAction<D extends ShowVersionsActionDefinition> extends AbstractVersionAction<D> {

    private final AppContext appContext;

    @Inject
    public ShowVersionsAction(D definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n) {
        super(definition, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n);
        this.appContext = appContext;
    }

    @Override
    protected Class getBeanItemClass() {
        return VersionName.class;
    }

    @Override
    protected FormDialogDefinition buildNewComponentDialog() throws ActionExecutionException, RepositoryException {
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();

        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("versions");

        SelectFieldDefinition select = new SelectFieldDefinition();
        select.setName("versionName");
        tab.addField(select);

        // All versions
        for (VersionInfo versionInfo : getAvailableVersionInfoList()) {
            SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
            option.setValue(versionInfo.getVersionName());
            option.setLabel(getVersionLabel(versionInfo));
            select.addOption(option);
        }

        ConfiguredFormDialogDefinition dialog = new ConfiguredFormDialogDefinition();
        dialog.setId("ui-contentapp:code:ShowVersionsAction.selectVersion");
        dialog.setForm(form);

        CallbackDialogActionDefinition callbackAction = new CallbackDialogActionDefinition();
        callbackAction.setName("commit");
        dialog.getActions().put(callbackAction.getName(), callbackAction);

        CancelDialogActionDefinition cancelAction = new CancelDialogActionDefinition();
        cancelAction.setName("cancel");
        dialog.getActions().put(cancelAction.getName(), cancelAction);

        form.addTab(tab);

        return dialog;
    }

    @Override
    protected Node getNode() throws RepositoryException {
        return nodeAdapter.getJcrItem();
    }

    @Override
    protected Location getLocation() throws ActionExecutionException {
        try {
            final Node node = getNode();
            final String path = node.getPath();
            final String appName = appContext.getName();
            return new DetailLocation(appName, "detail", DetailView.ViewType.VIEW, path, (String)getItem().getItemProperty("versionName").getValue());
        } catch (RepositoryException e) {
            throw new ActionExecutionException("Could not get node from nodeAdapter " + nodeAdapter.getItemId());
        }
    }

    /**
     * Simple POJO used to access user selection from dialog, see {@link com.vaadin.data.util.BeanItem}.
     */
    private class VersionName {
        private String versionName;

        private String getVersionName() {
            return versionName;
        }

        private void setVersionName(String versionName) {
            this.versionName = versionName;
        }
    }

}
