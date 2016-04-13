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
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.contentconnector.ContentConnectorProvider;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.framework.action.AbstractVersionAction;
import info.magnolia.ui.framework.action.EditorCallbackActionDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Opens a dialog with list of versions.
 *
 * @param <D> {@link ActionDefinition}.
 */
public class ShowVersionsAction<D extends ActionDefinition> extends AbstractVersionAction<D> {

    protected final AbstractJcrNodeAdapter nodeAdapter;
    private final ContentConnector contentConnector;
    private final AppContext appContext;

    protected String dialogID;

    @Inject
    public ShowVersionsAction(D definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, ContentConnector contentConnector) {
        super(definition, locationController, uiContext, formDialogPresenter, i18n);
        this.nodeAdapter = nodeAdapter;
        this.appContext = appContext;
        this.dialogID = "ui-contentapp:code:ShowVersionsAction.selectVersion";
        this.contentConnector = contentConnector;
    }

    /**
     * @deprecated since 5.4.6 - use {@link ShowVersionsAction(D, AppContext, LocationController, UiContext, FormDialogPresenter, AbstractJcrNodeAdapter, SimpleTranslator, ContentConnector)} instead.
     */
    @Deprecated
    public ShowVersionsAction(D definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n) {
        this(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n, getContentConnectorForDeprecations(uiContext));
    }

    /**
     * @deprecated since 5.3.5 - use {@link ShowVersionsAction(D, AppContext, LocationController, UiContext, FormDialogPresenter, AbstractJcrNodeAdapter, SimpleTranslator, ContentConnector)} instead.
     */
    @Deprecated
    public ShowVersionsAction(ShowVersionsActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n) {
        this((D) definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n, getContentConnectorForDeprecations(uiContext));
    }

    /**
     * Gets {@link ContentConnector} for deprecations from {@link UiContext}.
     * Cannot get by {@link Components#getComponent(Class)} due to the scope of {@link ContentConnector} is sub-app.
     * @see ContentConnectorProvider
     */
    protected static ContentConnector getContentConnectorForDeprecations(UiContext uiContext) {
        if (uiContext instanceof SubAppContext) {
            ContentConnectorProvider contentConnectorProvider = new ContentConnectorProvider((SubAppContext) uiContext, Components.getComponentProvider(), new SimpleEventBus());
            return contentConnectorProvider.get();
        } else {
            throw new IllegalStateException("Cannot get ContentConnector from " + uiContext);
        }
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
        select.setName(VersionName.PROPERTY_NAME_VERSION_NAME);
        select.setSortOptions(false);
        tab.addField(select);

        // All versions
        for (VersionInfo versionInfo : getAvailableVersionInfoList()) {
            SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
            option.setValue(versionInfo.getVersionName());
            option.setLabel(getVersionLabel(versionInfo));
            select.addOption(option);
        }

        ConfiguredFormDialogDefinition dialog = new ConfiguredFormDialogDefinition();
        dialog.setModalityLevel(getModalityLevel());
        dialog.setId(dialogID);
        dialog.setForm(form);

        EditorCallbackActionDefinition callbackAction = new EditorCallbackActionDefinition();
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
            Object itemId = contentConnector.getItemId(nodeAdapter);
            String path = contentConnector.getItemUrlFragment(itemId);
            return new DetailLocation(appContext.getName(), "detail", DetailView.ViewType.VIEW, path, getVersionName());
        } catch (Exception e) {
            throw new ActionExecutionException("Could not get location from nodeAdapter " + nodeAdapter.getItemId());
        }
    }

    protected String getVersionName() {
        return (String) getItem().getItemProperty(VersionName.PROPERTY_NAME_VERSION_NAME).getValue();
    }

    /**
     * Simple POJO used to access user selection from dialog, see {@link com.vaadin.data.util.BeanItem}.
     */
    protected class VersionName {

        protected final static String PROPERTY_NAME_VERSION_NAME = "versionName";

        private String versionName;

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }
    }

}
