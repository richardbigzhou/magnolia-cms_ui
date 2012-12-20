/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.pages.editor;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.admincentral.dialog.FormDialogPresenter;
import info.magnolia.ui.admincentral.dialog.FormDialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.app.pages.field.TemplateSelectorField;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.model.dialog.builder.DialogBuilder;
import info.magnolia.ui.model.dialog.builder.DialogConfig;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.form.builder.FieldsConfig;
import info.magnolia.ui.model.form.builder.FormBuilder;
import info.magnolia.ui.model.form.builder.FormConfig;
import info.magnolia.ui.model.form.builder.OptionBuilder;
import info.magnolia.ui.model.form.builder.SelectFieldBuilder;
import info.magnolia.ui.model.form.builder.TabBuilder;
import info.magnolia.ui.vaadin.editor.PageEditor;
import info.magnolia.ui.vaadin.editor.PageEditorParameters;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


/**
 * Presenter for the server side {@link PageEditorView}.
 * Serves multiple methods for actions triggered from the page editor.
 */
public class PageEditorPresenter implements PageEditorView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PageEditorPresenter.class);

    private final PageEditorView view;

    private final EventBus eventBus;

    private final FormDialogPresenterFactory dialogPresenterFactory;

    private final TemplateDefinitionRegistry templateDefinitionRegistry;

    private PageEditor.AbstractElement selectedElement;

    @Inject
    public PageEditorPresenter(PageEditorView view, @Named("subapp") EventBus eventBus, FormDialogPresenterFactory dialogPresenterFactory, TemplateDefinitionRegistry templateDefinitionRegistry) {
        this.view = view;
        this.eventBus = eventBus;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.templateDefinitionRegistry = templateDefinitionRegistry;

        registerHandlers();
    }

    private void registerHandlers() {
        eventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                view.refresh();
            }
        });
    }

    @Override
    public void editComponent(String workspace, String path, String dialog) {
        final FormDialogPresenter dialogPresenter = dialogPresenterFactory.createDialogPresenterByName(dialog);

        try {
            Session session = MgnlContext.getJCRSession(workspace);
            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            final Node node = session.getNode(path);
            final JcrNodeAdapter item = new JcrNodeAdapter(node);
            createDialogAction(item, dialogPresenter);
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates a chain of dialogs for creating new components.
     * The first dialog is built on the fly based on the available components passed from the client.
     * Based on the selection made in the first dialog the second dialog will be created, providing fields for the actual component.
     * @param workspace the workspace of the parent node
     * @param path the parent node path
     * @param availableComponents available components for the parent area
     */
    @Override
    public void newComponent(String workspace, String path, String availableComponents) {

        DialogDefinition dialogDefinition = buildNewComponentDialog(availableComponents);

        final FormDialogPresenter dialogPresenter = dialogPresenterFactory.createDialogPresenterByDefinition(dialogDefinition);
        try {
            Session session = MgnlContext.getJCRSession(workspace);

            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            session = MgnlContext.getJCRSession(workspace);

            Node parentNode = session.getNode(path);

            final JcrNodeAdapter item = new JcrNewNodeAdapter(parentNode, MgnlNodeType.NT_COMPONENT);
            DefaultProperty property = new DefaultProperty(ModelConstants.JCR_NAME, "0");
            item.addItemProperty(ModelConstants.JCR_NAME, property);

            // perform custom chaining of dialogs
            dialogPresenter.start(item, new FormDialogPresenter.Callback() {

                @Override
                public void onSuccess(String actionName) {
                    String templateId = String.valueOf(item.getItemProperty("mgnl:template").getValue());
                    try {
                        TemplateDefinition templateDef = templateDefinitionRegistry.getTemplateDefinition(templateId);
                        String dialog = templateDef.getDialog();
                        final FormDialogPresenter dialogPresenter = dialogPresenterFactory.createDialogPresenterByName(dialog);
                        createDialogAction(item, dialogPresenter);
                    } catch (RegistrationException e) {
                        log.error("Exception caught: {}", e.getMessage(), e);
                    } finally {
                        dialogPresenter.closeDialog();
                    }
                }

                @Override
                public void onCancel() {
                    dialogPresenter.closeDialog();
                }
            });

        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }

    }

    /**
     * Create a Dialog and define the call back actions.
     */
    private void createDialogAction(final JcrNodeAdapter item, final FormDialogPresenter dialogPresenter) {
        dialogPresenter.start(item, new FormDialogPresenter.Callback() {

            @Override
            public void onSuccess(String actionName) {
                eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getPath()));
                dialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                dialogPresenter.closeDialog();
            }
        });
    }

    /**
     * Builds a new {@link DialogDefinition} containing actions and {@link info.magnolia.ui.model.form.definition.FormDefinition}.
     * The definition will hold a {@link info.magnolia.ui.model.field.definition.SelectFieldDefinition} with the available components as options.
     */
    private DialogDefinition buildNewComponentDialog(String availableComponents) {

        DialogConfig dialogConfig = new DialogConfig();
        FormConfig formConfig = new FormConfig();
        FieldsConfig fieldsConfig = new FieldsConfig();

        DialogBuilder dialogBuilder = new DialogBuilder("newComponent");

        dialogBuilder.actions(
                dialogConfig.action("commit").label("choose").action(new CallbackDialogActionDefinition()),
                dialogConfig.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
        );

        FormBuilder formBuilder = formConfig.form().description("Select the Component to add to the page.");
        TabBuilder tabBuilder = formConfig.tab("Components").label("Components");
        SelectFieldBuilder fieldBuilder = fieldsConfig.select("mgnl:template").label("Component");

        String[] tokens = availableComponents.split(",");

        for (int i = 0; i < tokens.length; i++) {
            try {
                TemplateDefinition paragraphInfo = templateDefinitionRegistry.getTemplateDefinition(tokens[i]);

                fieldBuilder.options(
                        (new OptionBuilder())
                                .value(paragraphInfo.getId())
                                .label(TemplateSelectorField.getI18nTitle(paragraphInfo))
                );

            } catch (RegistrationException e) {
                log.error("Exception caught: {}", e.getMessage(), e);
            }

        }

        tabBuilder.fields(fieldBuilder);
        formBuilder.tabs(tabBuilder);
        dialogBuilder.form(formBuilder);
        return dialogBuilder.exec();


    }

    @Override
    public void deleteComponent(String workspace, String path) {

        int index = path.lastIndexOf("/");
        String parent = path.substring(0, index);

        try {
            Session session = MgnlContext.getJCRSession(workspace);

            Node parentNode = session.getNode(parent);
            session.removeItem(path);
            MetaDataUtil.updateMetaData(parentNode);
            session.save();
            view.refresh();

        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void newArea(String workspace, String nodeType, String path) {

        int index = path.lastIndexOf("/");
        String parent = path.substring(0, index);
        String relPath = path.substring(index + 1);

        Session session = null;
        try {
            session = MgnlContext.getJCRSession(workspace);

            Node parentNode = session.getNode(parent);

            Node newNode = NodeUtil.createPath(parentNode, relPath, nodeType);
            MetaDataUtil.updateMetaData(newNode);
            session.save();
            view.refresh();
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sortComponent(String workspace, String parentPath, String source, String target, String order) {
        try {

            if (StringUtils.isBlank(order)) {
                order = "before";
            }

            if (StringUtils.equalsIgnoreCase(target, "mgnlNew")) {
                target = null;
            }

            Session session = MgnlContext.getJCRSession(workspace);

            Node parent = session.getNode(parentPath);
            Node component = parent.getNode(source);

            if ("before".equals(order)) {
                NodeUtil.orderBefore(component, target);
            } else {
                NodeUtil.orderAfter(component, target);
            }

            MetaDataUtil.updateMetaData(parent);
            session.save();
            view.refresh();
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void selectElement(PageEditor.AbstractElement selectedElement) {
        this.selectedElement = selectedElement;
        eventBus.fireEvent(new NodeSelectedEvent(selectedElement.getPath(), selectedElement.getWorkspace()));
    }

    public PageEditorView start() {
        view.setListener(this);
        view.init();
        return view;
    }

    public PageEditor.AbstractElement getSelectedElement() {
        return selectedElement;
    }

    public void loadPageEditor(PageEditorParameters parameters) {
        view.load(parameters);
    }
}
