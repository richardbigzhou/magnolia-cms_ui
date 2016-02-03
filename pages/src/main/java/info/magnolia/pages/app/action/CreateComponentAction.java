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

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.pages.app.field.TemplateSelectorFieldFactory;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a component underneath the area passed in {@link AreaElement}.
 * Gets a list of available components for this area and creates a select field.
 */
public class CreateComponentAction extends AbstractAction<CreateComponentActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(CreateComponentAction.class);

    private AreaElement area;
    private EventBus eventBus;
    private TemplateDefinitionRegistry templateDefinitionRegistry;
    private SubAppContext subAppContext;
    private ComponentProvider componentProvider;
    private DialogView dialogView;
    private FormDialogPresenterFactory formDialogPresenterFactory;
    private final SimpleTranslator i18n;

    @Inject
    public CreateComponentAction(CreateComponentActionDefinition definition, AreaElement area, @Named(SubAppEventBus.NAME) EventBus eventBus, TemplateDefinitionRegistry templateDefinitionRegistry,
                                 SubAppContext subAppContext, ComponentProvider componentProvider, FormDialogPresenterFactory formDialogPresenterFactory, SimpleTranslator i18n) {
        super(definition);
        this.area = area;
        this.eventBus = eventBus;
        this.templateDefinitionRegistry = templateDefinitionRegistry;
        this.subAppContext = subAppContext;
        this.componentProvider = componentProvider;
        this.formDialogPresenterFactory = formDialogPresenterFactory;
        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {
        final FormDialogDefinition dialogDefinition = buildNewComponentDialog(area.getAvailableComponents());

        final FormDialogPresenter formDialogPresenter = componentProvider.newInstance(FormDialogPresenter.class);
        try {
            String workspace = area.getWorkspace();
            String path = area.getPath();
            Session session = MgnlContext.getJCRSession(area.getWorkspace());
            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            session = MgnlContext.getJCRSession(workspace);

            Node areaNode = session.getNode(path);

            final JcrNodeAdapter item = new JcrNewNodeAdapter(areaNode, NodeTypes.Component.NAME);
            DefaultProperty<String> property = new DefaultProperty<String>(String.class, "0");
            item.addItemProperty(ModelConstants.JCR_NAME, property);

            // perform custom chaining of dialogs
            this.dialogView = formDialogPresenter.start(item, dialogDefinition, subAppContext, new ComponentCreationCallback(item, formDialogPresenter));
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    private void openDialog(final JcrNodeAdapter item, String dialogId) {

        final FormDialogPresenter dialogPresenter = formDialogPresenterFactory.createFormDialogPresenter(dialogId);

        dialogPresenter.start(item, dialogId, subAppContext, new EditorCallback() {

            @Override
            public void onSuccess(String actionName) {
                eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
                dialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                dialogPresenter.closeDialog();
            }
        });
    }

    /**
     * Builds a new {@link info.magnolia.ui.dialog.definition.FormDialogDefinition} containing actions and {@link info.magnolia.ui.form.definition.FormDefinition}.
     * The definition will hold a {@link info.magnolia.ui.form.field.definition.SelectFieldDefinition} with the available components as options.
     */
    private FormDialogDefinition buildNewComponentDialog(String availableComponents) {

        ConfiguredFormDefinition form = new ConfiguredFormDefinition();

        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("components");

        SelectFieldDefinition select = new SelectFieldDefinition();
        select.setName("mgnl:template");

        tab.addField(select);

        form.addTab(tab);

        String[] tokens = availableComponents.split(",");

        for (String token : tokens) {
            try {
                TemplateDefinition templateDefinition = templateDefinitionRegistry.getTemplateDefinition(token);

                SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                option.setValue(templateDefinition.getId());
                option.setLabel(TemplateSelectorFieldFactory.getI18nTitle(templateDefinition));
                select.addOption(option);

            } catch (RegistrationException e) {
                log.error("Exception caught: {}", e.getMessage(), e);
            }
        }

        ConfiguredFormDialogDefinition dialog = new ConfiguredFormDialogDefinition();
        dialog.setId("pages:newComponent");
        dialog.setForm(form);

        CallbackDialogActionDefinition callbackAction = new CallbackDialogActionDefinition();
        callbackAction.setName("commit");
        dialog.getActions().put(callbackAction.getName(), callbackAction);

        CancelDialogActionDefinition cancelAction = new CancelDialogActionDefinition();
        cancelAction.setName("cancel");
        dialog.getActions().put(cancelAction.getName(), cancelAction);

        return dialog;
    }

    private class ComponentCreationCallback implements EditorCallback {

        private final JcrNodeAdapter item;
        private final FormDialogPresenter formDialogPresenter;

        public ComponentCreationCallback(JcrNodeAdapter item, FormDialogPresenter formDialogPresenter) {
            this.item = item;
            this.formDialogPresenter = formDialogPresenter;
        }

        @Override
        public void onSuccess(String actionName) {
            String templateId = String.valueOf(item.getItemProperty(NodeTypes.Renderable.TEMPLATE).getValue());
            try {
                TemplateDefinition templateDef = templateDefinitionRegistry.getTemplateDefinition(templateId);
                String dialogId = templateDef.getDialog();

                if (StringUtils.isNotEmpty(dialogId)) {
                    openDialog(item, dialogId);
                } else {
                    // if there is no dialog defined for the component, persist the node as is and reload.
                    try {
                        final Node node = item.applyChanges();
                        node.getSession().save();

                    } catch (RepositoryException e) {
                        log.error("Exception caught: {}", e.getMessage(), e);
                    }

                    eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
                }
            } catch (RegistrationException e) {
                log.error("Exception caught: {}", e.getMessage(), e);
            } finally {
                dialogView.close();
            }
        }

        @Override
        public void onCancel() {
            formDialogPresenter.closeDialog();
        }
    }
}
