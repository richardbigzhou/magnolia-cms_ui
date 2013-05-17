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

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.dialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
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
 * Presenter for the server side {@link PageEditorView}.
 * Serves multiple methods for actions triggered from the page editor.
 */
public class PageEditorPresenter implements PageEditorView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PageEditorPresenter.class);

    private final ActionExecutor actionExecutor;
    private final PageEditorView view;

    private final EventBus subAppEventBus;

    private final TemplateDefinitionRegistry templateDefinitionRegistry;

    private AbstractElement selectedElement;

    private final SubAppContext subAppContext;

    private final ComponentProvider componentProvider;

    @Inject
    public PageEditorPresenter(final ActionExecutor actionExecutor, PageEditorView view, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, TemplateDefinitionRegistry templateDefinitionRegistry,
            SubAppContext subAppContext, ComponentProvider componentProvider) {
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.subAppEventBus = subAppEventBus;
        this.templateDefinitionRegistry = templateDefinitionRegistry;
        this.subAppContext = subAppContext;
        this.componentProvider = componentProvider;
        registerHandlers();
    }

    private void registerHandlers() {
        subAppEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (event.getWorkspace().equals(RepositoryConstants.WEBSITE)) {
                        view.refresh();
                }
            }
        });
    }

    @Override
    public void editComponent(String workspace, String path, String dialogName) {
        final FormDialogPresenter formDialogPresenter = componentProvider.getComponent(FormDialogPresenter.class);

        try {
            Session session = MgnlContext.getJCRSession(workspace);
            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            final Node node = session.getNode(path);
            final JcrNodeAdapter item = new JcrNodeAdapter(node);
            openDialog(item, dialogName, formDialogPresenter);
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates a chain of dialogs for creating new components.
     * The first dialog is built on the fly based on the available components passed from the client.
     * Based on the selection made in the first dialog the second dialog will be created, providing fields for the actual component.
     */
    @Override
    public void newComponent() {
        try {
            actionExecutor.execute("addComponent", selectedElement);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            subAppContext.getAppContext().broadcastMessage(error);
        }
    }

    @Override
    public void newArea() {
        try {
            actionExecutor.execute("addArea", selectedElement);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            subAppContext.getAppContext().broadcastMessage(error);
        }

    }

    /**
     * Create a Dialog and define the call back actions.
     */
    private void openDialog(final JcrNodeAdapter item, final String dialogName, final FormDialogPresenter formDialogPresenter) {

        formDialogPresenter.start(item, dialogName, subAppContext, new EditorCallback() {

            @Override
            public void onSuccess(String actionName) {
                subAppEventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        });
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

            NodeTypes.LastModified.update(parent);
            session.save();
            view.refresh();
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
    }

    @Override
    public void selectElement(AbstractElement selectedElement) {
        this.selectedElement = selectedElement;
        subAppEventBus.fireEvent(new NodeSelectedEvent(selectedElement));
    }

    public PageEditorView start() {
        view.setListener(this);
        view.init();
        return view;
    }

    public AbstractElement getSelectedElement() {
        return selectedElement;
    }

    public void loadPageEditor(PageEditorParameters parameters) {
        view.load(parameters);
    }

    public void updateParameters(PageEditorParameters parameters) {
        view.update(parameters);
    }

    public TemplateDefinitionRegistry getTemplateDefinitionRegistry() {
        return templateDefinitionRegistry;
    }


}
